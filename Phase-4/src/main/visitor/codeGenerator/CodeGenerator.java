package main.visitor.codeGenerator;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.nio.file.*;
import java.util.logging.Logger;
import main.ast.nodes.Program;
import main.ast.nodes.declaration.FunctionDeclaration;
import main.ast.nodes.declaration.MainDeclaration;
import main.ast.nodes.declaration.VarDeclaration;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.operators.BinaryOperator;
import main.ast.nodes.expression.operators.UnaryOperator;
import main.ast.nodes.expression.value.FunctionPointer;
import main.ast.nodes.expression.value.ListValue;
import main.ast.nodes.expression.value.primitive.BoolValue;
import main.ast.nodes.expression.value.primitive.IntValue;
import main.ast.nodes.expression.value.primitive.StringValue;
import main.ast.nodes.statement.*;
import main.ast.type.FptrType;
import main.ast.type.ListType;
import main.ast.type.NoType;
import main.ast.type.Type;
import main.ast.type.primitiveType.BoolType;
import main.ast.type.primitiveType.IntType;
import main.ast.type.primitiveType.StringType;
import main.symbolTable.SymbolTable;
import main.symbolTable.exceptions.ItemAlreadyExists;
import main.symbolTable.exceptions.ItemNotFound;
import main.symbolTable.item.FunctionItem;
import main.symbolTable.item.VarItem;
import main.visitor.Visitor;
import main.visitor.type.TypeChecker;

public class CodeGenerator extends Visitor<String> {

	private final String outputPath;
	private final TypeChecker typeChecker;
	private final Set<String> visited;

	private static final Logger log = Logger.getLogger(TypeChecker.class.getName());

	private FileWriter mainFile;
	private FunctionItem curFunction;

	private final HashMap<String, Integer> slots = new HashMap<>();
	private int curLabel = 0;
	private Stack<String> nextLabels = new Stack<>();
	private Stack<String> breakLabels = new Stack<>();

	public CodeGenerator(TypeChecker typeChecker) {
		this.typeChecker = typeChecker;
		this.visited = typeChecker.visited;
		outputPath = "./codeGenOutput/";
		prepareOutputFolder();
	}

	private int slotOf(String var) {
		if (!slots.containsKey(var)) {
			slots.put(var, slots.size());
			return slots.size() - 1;
		}
		return slots.get(var);
	}

	private String getFreshName() {
		String fresh = "Var_" + slots.size();
		slotOf(fresh);
		return fresh;
	}

	private String getFreshLabel() {
		String fresh = "Label_" + curLabel;
		curLabel++;
		return fresh;
	}

	private String getType(Type element) {
		String type = "";
		switch (element) {
			case NoType noType 		   -> type += "V";
			case StringType stringType -> type += "Ljava/lang/String;";
			case IntType intType       -> type += "Ljava/lang/Integer;";
			case BoolType boolType     -> type += "Ljava/lang/Boolean;";
			case FptrType fptrType     -> type += "LFptr;";
			case ListType listType     -> type += "LList;";
			case null, default 		   -> type += "V";
		}
		return type;
	}

	private String getTypeSignature(Type element) {
		String type = "";
		switch (element) {
			case NoType noType 		   -> type += "V";
			case StringType stringType -> type += "Ljava/lang/String;";
			case IntType intType       -> type += "I";
			case BoolType boolType     -> type += "Z";
			case FptrType fptrType     -> type += "LFptr;";
			case ListType listType     -> type += "LList;";
			case null, default 		   -> type += "V";
		}
		return type;
	}

	private String getEquivalentJVMType(Type element) {
		String type = "";
		switch (element) {
			case StringType stringType -> type += "java/lang/String";
			case IntType intType       -> type += "java/lang/Integer";
			case BoolType boolType     -> type += "java/lang/Boolean";
			case null, default 		   -> type += "java/lang/Object";
		}
		return type;
	}

	public void cleanMainJasminFile() {
		String fileName = "./codeGenOutput/Main.j";
        Path filePath = Paths.get(fileName);
        try {
            List<String> content = Files.readAllLines(filePath);
            List<String> newContent = new ArrayList<>();
            for (String line : content) {
				if (Pattern.compile("^\\s*\\.method").matcher(line).find()) {
                    newContent.add("\n\n" + line.replaceAll("^\\s+", ""));
                } else if (Pattern.compile("^\\s*\\.").matcher(line).find()) {
                    newContent.add(line.replaceAll("^\\s+", ""));
                } else if (Pattern.compile("^\\s*Label").matcher(line).find()) {
                    newContent.add(line.replaceAll("^\\s+", "\t"));
                } else {
                    newContent.add(line);
                }
            }
            Files.write(filePath, newContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	private void prepareOutputFolder() {
		final String jasminPath    = "utilities/jarFiles/jasmin.jar";
		final String listClassPath = "utilities/codeGenerationUtilityClasses/List.j";
		final String fptrClassPath = "utilities/codeGenerationUtilityClasses/Fptr.j";
		
		try {
			File directory = new File(this.outputPath);
			File[] files = directory.listFiles();
			if (files != null) {
				for (File file : files) {
					file.delete();
				}
			} 
			directory.mkdir();
		} catch (SecurityException e) {
			e.printStackTrace();
		}

		copyFile(jasminPath,    this.outputPath + "jasmin.jar");
		copyFile(listClassPath, this.outputPath + "List.j");
		copyFile(fptrClassPath, this.outputPath + "Fptr.j");

		try {
			String path = outputPath + "Main.j";
			File file = new File(path);
			file.createNewFile();
			mainFile = new FileWriter(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void copyFile(String toBeCopied, String toBePasted) {
		try {
			File readingFile = new File(toBeCopied);
			File writingFile = new File(toBePasted);
			InputStream readingFileStream = new FileInputStream(readingFile);
			OutputStream writingFileStream = new FileOutputStream(writingFile);
			byte[] buffer = new byte[1024];
			int readLength;
			while ((readLength = readingFileStream.read(buffer)) > 0) {
				writingFileStream.write(buffer, 0, readLength);
			}
			readingFileStream.close();
			writingFileStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addCommand(String command) {
		try {
			command = String.join("\n\t\t", command.split("\n"));

			if (command.startsWith("Label_")) {
				mainFile.write("\t" + command + "\n"); 
			} 
			else if (command.startsWith(".")) {
				mainFile.write(command + "\n"); 
			}
			else {
				mainFile.write("\t\t" + command + "\n");
			}
			mainFile.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleMainClass() {
		String mainCommands =
			"""
			.method public static main([Ljava/lang/String;)V
			.limit stack 128
			.limit locals 128
			new Main
			invokespecial Main/<init>()V
			return
			.end method\n\n
			""";
		addCommand(mainCommands);
	}

	@Override
	public String visit(Program program) {
		String commands = 
			"""
			.class public Main
			.super java/lang/Object
			""";

		addCommand(commands);
		handleMainClass();

		for (String funcName : this.visited) {
			try {
				this.curFunction = (FunctionItem) SymbolTable.root.getItem(FunctionItem.START_KEY + funcName);
				this.curFunction.getFunctionDeclaration().accept(this);
			} catch (ItemNotFound ignored) {}
		}

		program.getMain().accept(this);
		return null;
	}

	@Override
	public String visit(MainDeclaration mainDeclaration) {
		slots.clear();
		String commands = 
			"""
			.method public <init>()V
			.limit stack 128
			.limit locals 128
			aload_0
			invokespecial java/lang/Object/<init>()V
			""";

		// save slot 0 for this
		slotOf("this");

		for (var statement : mainDeclaration.getBody()) {
			String temp = statement.accept(this);
			if (temp != null) {
				commands += temp;
			}
		}

		commands += "return\n";
		commands += ".end method\n\n"; // few new lines for readability

		addCommand(commands);
		return null;
	}

	@Override
	public String visit(FunctionDeclaration functionDeclaration) {
		slots.clear();
		String commands = "";

		String args = "(";
		Type returnType = new NoType();

		SymbolTable.push(SymbolTable.top.copy());
		try {
			FunctionItem functionItem = (FunctionItem) SymbolTable.root.getItem(
				FunctionItem.START_KEY + 
				functionDeclaration.getFunctionName().getName()
			);
			returnType = functionItem.getReturnType();

			ArrayList<Type> currentArgTypes = functionItem.getArgumentTypes();
			for (int i = 0; i < functionDeclaration.getArgs().size(); i++) {
				VarItem argItem = new VarItem(functionDeclaration.getArgs().get(i).getName());
				argItem.setType(currentArgTypes.get(i));
				try {
					SymbolTable.top.put(argItem);
				} catch (ItemAlreadyExists ignored) {
					var item = (VarItem) SymbolTable.top.getItem(VarItem.START_KEY + argItem.getName());
					item.setType(currentArgTypes.get(i));
				}
			}
			ArrayList<Type> argTypes = functionItem.getArgumentTypes();
			ArrayList<VarDeclaration> argDeclarations = functionDeclaration.getArgs();

			for (int i = 0; i < argTypes.size(); i++) {
				Type argType = argTypes.get(i);
				VarDeclaration argDeclaration = argDeclarations.get(i);
				args += getType(argType);
				slotOf(argDeclaration.getName().getName());
			}

		} catch (ItemNotFound ignored) {}

		args += ")";

		String returnTypeString = getType(returnType);
		commands += ".method public static " + functionDeclaration.getFunctionName().getName();
		commands += args + returnTypeString + "\n";

		commands += 
			"""
			.limit stack 128
			.limit locals 128
			""";

		for (var statement : functionDeclaration.getBody()) {
			String temp = statement.accept(this);
			if (temp != null) {
				commands += temp;
			}
		}

		commands += "return" + "\n";
		commands += ".end method\n\n"; // few new lines for readability
		SymbolTable.pop();
		addCommand(commands);
		return null;
	}

	public String visit(AccessExpression accessExpression) {
		String commands = "";
		Identifier accessedIdentifier = (Identifier) accessExpression.getAccessedExpression();
		Type accessedType = accessedIdentifier.accept(typeChecker);
		String functionName;
		if (accessExpression.isFunctionCall()) {
			if (accessedType instanceof FptrType fptr) { // function pointer
				functionName = fptr.getFunctionName();
			} 
			else { // normal function 
				functionName = accessedIdentifier.getName();
			}
			String args = "(";
			for (var arg : accessExpression.getArguments()) {
				Type argType = arg.accept(typeChecker);
				commands += arg.accept(this);
				args += getType(argType);
			}

			String returnType = "";
			
			try {
				FunctionItem functionItem = (FunctionItem) SymbolTable.root.getItem(
					FunctionItem.START_KEY + 
					functionName
				);
				for (int i = accessExpression.getArguments().size(); i < functionItem.getArgumentTypes().size(); i++) {
					args += getType(functionItem.getArgumentTypes().get(i));
					commands += functionItem.getFunctionDeclaration().getArgs().get(i).getDefaultVal().accept(this);
				}
				returnType = getType(functionItem.getReturnType());
			} catch (ItemNotFound ignored) {}

			args += ")";

			return (
				commands +
				"invokestatic Main/" +
				functionName +
				args +
				returnType +
				"\n"
			);
		} else { // access to a list
			String listName = accessedIdentifier.getName();
			int slot = slotOf(listName);
			commands += "aload " + slot + "\n";
			commands += accessExpression.getDimentionalAccess().get(0).accept(this);
			commands += "invokevirtual java/lang/Integer/intValue()I\n";
			commands += "invokevirtual List/getElement(I)Ljava/lang/Object;" + "\n";
			Type listType = accessedIdentifier.accept(typeChecker);
			if (listType instanceof ListType list) {
				commands += "checkcast " + getEquivalentJVMType(list.getType()) + "\n";
			}
			return commands;
		}
	}

	@Override
	public String visit(AssignStatement assignStatement) {
		String commands = "";
		AssignOperator assignOperator = assignStatement.getAssignOperator();
		if (assignStatement.isAccessList()) {
			String listName = assignStatement.getAssignedId().getName();
			int slot = slotOf(listName);
			commands += "aload " + slot + "\n";
			commands += assignStatement.getAccessListExpression().accept(this);
			commands += "invokevirtual java/lang/Integer/intValue()I\n";

			if (assignOperator != AssignOperator.ASSIGN) {
				commands += "aload " + slot + "\n";
				commands += assignStatement.getAccessListExpression().accept(this);
				commands += "invokevirtual java/lang/Integer/intValue()I\n";
				commands += "invokevirtual List/getElement(I)Ljava/lang/Object;" + "\n";
				commands += "checkcast java/lang/Integer\n";
				commands += "invokevirtual java/lang/Integer/intValue()I\n";

				commands += assignStatement.getAssignExpression().accept(this);
				commands += "invokevirtual java/lang/Integer/intValue()I\n";

				switch (assignOperator) {
					case PLUS_ASSIGN   -> commands += "iadd\n";
					case MINUS_ASSIGN  -> commands += "isub\n";
					case MULT_ASSIGN   -> commands += "imul\n";
					case DIVIDE_ASSIGN -> commands += "idiv\n";
					case MOD_ASSIGN    -> commands += "irem\n";
					case null, default -> {}
				}
				commands += "invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;\n";
				
			} else {
				commands += assignStatement.getAssignExpression().accept(this);
			}

			commands += "invokevirtual List/setElement(ILjava/lang/Object;)V" + "\n";
			return commands;
		} 
		else {
			Identifier assignedId = assignStatement.getAssignedId();
			Expression assignExpression = assignStatement.getAssignExpression();
			
			Type assignExpType = assignStatement.getAssignExpression().accept(typeChecker);
			VarItem newVarItem = new VarItem(assignStatement.getAssignedId());
			newVarItem.setType(assignExpType);
			try {
				SymbolTable.top.put(newVarItem);
			} catch (ItemAlreadyExists ignore) {}
		
			String varName = assignedId.getName();
			int slot = slotOf(varName);
			
			if (assignOperator == AssignOperator.ASSIGN) {
				commands += assignExpression.accept(this);
				commands += "astore " + slot + "\n";
			}
			else {
				commands += "aload " + slot + "\n";
				commands += "invokevirtual java/lang/Integer/intValue()I\n";

				commands += assignExpression.accept(this);
				commands += "invokevirtual java/lang/Integer/intValue()I\n";

				switch (assignOperator) {
					case PLUS_ASSIGN   -> commands += "iadd\n";
					case MINUS_ASSIGN  -> commands += "isub\n";
					case MULT_ASSIGN   -> commands += "imul\n";
					case DIVIDE_ASSIGN -> commands += "idiv\n";
					case MOD_ASSIGN    -> commands += "irem\n";
					case null, default -> {}
				}
				
				commands += "invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;\n";
				commands += "astore " + slot + "\n";
			}
			return commands;
		}
	}

	@Override
	public String visit(IfStatement ifStatement) {
		String commands = "";
		String thenLabel = getFreshLabel();
		String endLabel = getFreshLabel();

		for (var condition : ifStatement.getConditions()) { 
			commands += condition.accept(this);

			if (condition instanceof BinaryExpression binaryExpression) {
				if (binaryExpression.getOperator() == BinaryOperator.EQUAL || 
					binaryExpression.getOperator() == BinaryOperator.NOT_EQUAL ||
					binaryExpression.getOperator() == BinaryOperator.LESS_THAN ||
					binaryExpression.getOperator() == BinaryOperator.LESS_EQUAL_THAN ||
					binaryExpression.getOperator() == BinaryOperator.GREATER_THAN ||
					binaryExpression.getOperator() == BinaryOperator.GREATER_EQUAL_THAN) {
						commands += thenLabel + "\n"; 
				} // no need to check for else
			} else {
				commands += "invokevirtual java/lang/Boolean/booleanValue()Z\n";
				log.info(condition.toString());
				commands += "ifne " + thenLabel + "\n"; 
			}
		}

		SymbolTable.push(SymbolTable.top.copy());
		for (var statement : ifStatement.getElseBody()) {
			String temp = statement.accept(this);
			if (temp != null) {
				commands += temp;
			}
		}
		SymbolTable.pop();
		
		commands += "goto " + endLabel + "\n";
		commands += thenLabel + ":\n";
		
		SymbolTable.push(SymbolTable.top.copy());
		for (var statement : ifStatement.getThenBody()) {
			String temp = statement.accept(this);
			if (temp != null) {
				commands += temp;
			}
		}
		SymbolTable.pop();
		
		commands += endLabel + ":\n";

		return commands;
	}

	@Override
	public String visit(PutStatement putStatement) {
		String commands = "";
		commands += "getstatic java/lang/System/out Ljava/io/PrintStream;\n";
		commands += putStatement.getExpression().accept(this);
		Type type = putStatement.getExpression().accept(typeChecker);
		if (type instanceof IntType) {
			commands += "invokevirtual java/lang/Integer/intValue()I\n";
		} else if (type instanceof BoolType) {
			commands += "invokevirtual java/lang/Boolean/booleanValue()Z\n";
		} 
		commands += "invokevirtual java/io/PrintStream/println(" + 
					getTypeSignature(putStatement.getExpression().accept(typeChecker)) + ")V\n";
		return commands;
	}

	@Override
	public String visit(ReturnStatement returnStatement) {
		String commands = "";
		
		if (!returnStatement.hasRetExpression()) {
			commands += "return\n";
		}
		else {
			commands += returnStatement.getReturnExp().accept(this);
			commands += "areturn\n";
		}
		return commands;
	}

	@Override
	public String visit(ExpressionStatement expressionStatement) {
		return expressionStatement.getExpression().accept(this);
	}

	@Override
	public String visit(BinaryExpression binaryExpression) {
		String commands = "";
		commands += binaryExpression.getFirstOperand().accept(this);
		commands += "invokevirtual java/lang/Integer/intValue()I" + "\n";
		commands += binaryExpression.getSecondOperand().accept(this);
		commands += "invokevirtual java/lang/Integer/intValue()I" + "\n";
		BinaryOperator operator = binaryExpression.getOperator();
		String cast = "invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;\n";
		switch (operator) {
			// we only have int and bool types so we don't need to check
			case PLUS		        -> commands += "iadd\n" + cast;
			case MINUS		        -> commands += "isub\n" + cast;
			case MULT		        -> commands += "imul\n" + cast;
			case DIVIDE		        -> commands += "idiv\n" + cast;
			case EQUAL		        -> commands += "if_icmpeq ";
			case NOT_EQUAL	        -> commands += "if_icmpne ";
			case LESS_THAN	        -> commands += "if_icmplt ";
			case LESS_EQUAL_THAN    -> commands += "if_icmple ";
			case GREATER_THAN       -> commands += "if_icmpgt ";
			case GREATER_EQUAL_THAN -> commands += "if_icmpge ";
			case null, default      -> {}
		}
		return commands;
	}

	@Override
	public String visit(UnaryExpression unaryExpression) {
		String commands = "";
		commands += unaryExpression.getExpression().accept(this);

		if (unaryExpression.getOperator() == UnaryOperator.NOT) {
			commands += "invokevirtual java/lang/Boolean/booleanValue()Z" + "\n";
		} else {
			commands += "invokevirtual java/lang/Integer/intValue()I" + "\n";
		}

		String cast = "invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;\n";
		String castBool = "invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;\n";

		String store = "";
		if (unaryExpression.getExpression() instanceof Identifier identifier) {
			String varName = identifier.getName();
			int slot = slotOf(varName);
			store = "astore " + slot + "\n";
		}
		switch (unaryExpression.getOperator()) {
			case MINUS         -> commands += "ineg\n" + cast;
			case NOT           -> commands += "iconst_1\nixor\n" + castBool;
			case INC 		   -> commands += "iconst_1\niadd\n" + cast + store;
			case DEC 		   -> commands += "iconst_1\nisub\n" + cast + store;
			case null, default -> {}
		}
		return commands;
	}

	@Override
	public String visit(Identifier identifier) {
		String varName = identifier.getName();
		int slot = slotOf(varName);
		Type varType = identifier.accept(typeChecker);
		String commands = "aload " + slot + "\n";
		// if (varType instanceof IntType || varType instanceof BoolType) {
		// 	commands += "iload " + slot + "\n";
		// } else {
		// 	commands += "aload " + slot + "\n";
		// }
		return commands;
	}

	@Override
	public String visit(LoopDoStatement loopDoStatement) {
		String commands = "";
		
		String startLabel = getFreshLabel();
		String endLabel = getFreshLabel();

		breakLabels.push(endLabel);
		nextLabels.push(startLabel);

		commands += startLabel + ":\n";

		SymbolTable.push(SymbolTable.top.copy());		
		for (var statement : loopDoStatement.getLoopBodyStmts()) {
			String temp = statement.accept(this);
			if (temp != null) {
				commands += temp;
			}
		}
		SymbolTable.pop();

		commands += "goto " + startLabel + "\n";
		
		breakLabels.pop();
		nextLabels.pop();

		commands += endLabel + ":\n";

		return commands;
	}

	@Override
	public String visit(BreakStatement breakStatement) {
		String commands = "goto " + breakLabels.peek() + "\n";
		return commands;
	}

	@Override
	public String visit(NextStatement nextStatement) {
		String commands = "goto " + nextLabels.peek() + "\n";
		return commands;
	}

	@Override
	public String visit(LenStatement lenStatement) {
		Type lenType = lenStatement.getExpression().accept(typeChecker);
		String commands = "";
		commands += lenStatement.getExpression().accept(this);
		
		if (lenType instanceof StringType) { 
			commands += "invokevirtual java/lang/String/length()I\n";
		} else if (lenType instanceof ListType) {
			commands += "invokevirtual List/size()I\n";
		} 

		return commands;
	}

	@Override
	public String visit(ChopStatement chopStatement) {
		String commands = "";
		String exp = chopStatement.getChopExpression().accept(this);
		commands += exp;
		commands += "iconst_0\n";
		commands += exp;
		commands += "invokevirtual java/lang/String/length()I\n";
		commands += "iconst_1\n";
		commands += "isub\n";
		commands += "invokevirtual java/lang/String/substring(II)Ljava/lang/String;\n";
		return commands;
	}

	@Override
	public String visit(FunctionPointer functionPointer) {
		FptrType fptr = (FptrType) functionPointer.accept(typeChecker);
		String commands = "";
		commands += "new Fptr\n";
		commands += "dup\n";
		commands += "aload_0\n";
		commands += "ldc " + "\"" + fptr.getFunctionName() + "\"\n";
		commands += "invokespecial Fptr/<init>(Ljava/lang/Object;Ljava/lang/String;)V\n";
		return commands;
	}

	@Override
	public String visit(ListValue listValue) {
		String commands = "";
		commands += "new List" + "\n";
		commands += "dup" + "\n";
		commands += "new java/util/ArrayList" + "\n";
		commands += "dup" + "\n";
		commands += "invokespecial java/util/ArrayList/<init>()V" + "\n";
		
		int slot = slotOf("temp");
		commands += "astore " + slot + "\n";
		for (var element : listValue.getElements()) {
			commands += "aload " + slot + "\n";
			commands += element.accept(this);
			commands +=  "invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z" + "\n";
			commands += "pop" + "\n";
		}
		commands += "aload " + slot + "\n";
		commands += "invokespecial List/<init>(Ljava/util/ArrayList;)V" + "\n";
		// slots.remove("temp");
		return commands;
	}

	@Override
	public String visit(IntValue intValue) {
		String commands = "";
		commands += "ldc " + intValue.getIntVal() + "\n";
		commands += "invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;" + "\n";
		return commands;
	}
		
	@Override
	public String visit(BoolValue boolValue) {
		String commands = "";
		commands += "ldc " + boolValue.getIntValue() + "\n";
		commands += "invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;" + "\n";
		return commands;
	}

	@Override
	public String visit(StringValue stringValue) {
		String commands = "";
		commands += "ldc " + stringValue.getStr() + "\n";
		return commands;
	}
}
