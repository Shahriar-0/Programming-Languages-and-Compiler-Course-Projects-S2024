package main.visitor.codeGenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.Stack;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;
import main.ast.nodes.Program;
import main.ast.nodes.declaration.FunctionDeclaration;
import main.ast.nodes.declaration.MainDeclaration;
import main.ast.nodes.declaration.VarDeclaration;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.operators.BinaryOperator;
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
import main.symbolTable.exceptions.ItemNotFound;
import main.symbolTable.item.FunctionItem;
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
			case StringType stringType -> type += "Ljava/lang/String;";
			case IntType intType       -> type += "Ljava/lang/Integer;";
			case BoolType boolType     -> type += "Ljava/lang/Boolean;";
			case FptrType fptrType     -> type += "LFptr;";
			case ListType listType     -> type += "LList;";
			case null, default 		   -> {}
		}
		return type;
	}

	private String getTypeSignature(Type element) {
		String type = "";
		switch (element) {
			case StringType stringType -> type += "Ljava/lang/String;";
			case IntType intType       -> type += "I";
			case BoolType boolType     -> type += "Z";
			case FptrType fptrType     -> type += "LFptr;";
			case ListType listType     -> type += "LList;";
			case null, default 		   -> {}
		}
		return type;
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
			// ignore
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
			// ignore
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
			// ignore
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
			// ignore
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

	private ArrayList<Object> convertLastNArgumentsToObjects(int n) {
		// I dunno use store and load to implement it maybe?
		return null;
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

		for (var statement : mainDeclaration.getBody()) {
			// commands += statement.accept(this);
			
			String temp = statement.accept(this); // FIXME: this is a temporary fix till we implement the rest of the methods
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

		try {
			FunctionItem functionItem = (FunctionItem) SymbolTable.root.getItem(
				FunctionItem.START_KEY + 
				functionDeclaration.getFunctionName().getName()
			);

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

		Type returnType = functionDeclaration.accept(typeChecker);
		String returnTypeString = getType(returnType);

		commands += ".method public " + functionDeclaration.getFunctionName().getName();
		commands += args + returnTypeString + "\n";

		commands += 
			"""
			.limit stack 128
			.limit locals 128
			""";

		for (var statement : functionDeclaration.getBody()) {
			// commands += statement.accept(this);
			
			String temp = statement.accept(this); // FIXME: this is a temporary fix till we implement the rest of the methods
			if (temp != null) {
				commands += temp;
			}
		}

		if (returnType instanceof NoType) {
			commands += "return\n";
		} else if (returnType instanceof IntType || returnType instanceof BoolType) {
			commands += "ireturn\n";
		} else {
			commands += "areturn\n";
		}

		commands += ".end method\n\n"; // few new lines for readability

		addCommand(commands);
		return null;
	}

	public String visit(AccessExpression accessExpression) {
		Type accessedType = accessExpression.accept(typeChecker);

		if (accessExpression.isFunctionCall()) {
			if (accessedType instanceof FptrType) { // function pointer
				FunctionPointer functionPointer = (FunctionPointer) accessExpression.getAccessedExpression();
				// TODO: dunno what to do with this yet
			} 
			
			else { // normal function 
				Identifier accessedIdentifier = (Identifier) accessExpression.getAccessedExpression();
				String functionName = accessedIdentifier.getName();
				String args = "(";
				for (var arg : accessExpression.getArguments()) {
					args += arg.accept(this);
				}
				args += ")";

				String returnType = "return"; // default return type, this shouldn't happen but just in case
				
				try {
					FunctionItem functionItem = (FunctionItem) SymbolTable.root.getItem(
						FunctionItem.START_KEY + 
						functionName
					);
					returnType = getType(functionItem.getFunctionDeclaration().accept(typeChecker));
				} catch (ItemNotFound ignored) {}

				// TODO: i think we can pop here if it's not an rvalue

				return (
					"invokestatic Main/" +
					functionName +
					args +
					returnType +
					"\n"
				);
			}
			

		} else { // access to a list
			// TODO: dunno what to do with this yet
		}
		return null;
	}

	@Override
	public String visit(AssignStatement assignStatement) {
		if (assignStatement.isAccessList()) {
			// TODO: dunno what to do with this yet
			// NOTE: be careful to use iastore and iaload for arrays
			return null;
		} 
		else {
			Identifier assignedId = assignStatement.getAssignedId();
			Expression assignExpression = assignStatement.getAssignExpression();
			AssignOperator assignOperator = assignStatement.getAssignOperator();

			String varName = assignedId.getName();
			int slot = slotOf(varName);
			Type varType = assignStatement.getAssignedId().accept(typeChecker);
			
			
			String commands = "";
			if (assignOperator == AssignOperator.ASSIGN) {
				commands += assignExpression.accept(this);
				if (varType instanceof IntType || varType instanceof BoolType) {
					commands += "istore " + slot + "\n";
				} else {
					commands += "astore " + slot + "\n";
				}
			}
			else {
				if (varType instanceof IntType || varType instanceof BoolType) {
					commands += "iload " + slot + "\n";
				} else {
					commands += "aload " + slot + "\n";
				}

				commands += assignExpression.accept(this);
				switch (assignOperator) {
					case PLUS_ASSIGN   -> commands += "iadd\n";
					case MINUS_ASSIGN  -> commands += "isub\n";
					case MULT_ASSIGN   -> commands += "imul\n";
					case DIVIDE_ASSIGN -> commands += "idiv\n";
					case MOD_ASSIGN    -> commands += "irem\n";
					case null, default -> {}
				}
				
				if (varType instanceof IntType || varType instanceof BoolType) {
					commands += "istore " + slot + "\n";
				} else {
					commands += "astore " + slot + "\n";
				}
			}
			return commands;
		}
	}

	@Override
	public String visit(IfStatement ifStatement) {
		// not sure this is right at all
		String commands = "";
		String elseLabel = getFreshLabel();
		String endLabel = getFreshLabel();

		for (var condition : ifStatement.getConditions()) { 
			// FIXME: there is no where that they store && or || in the AST :)))))))))))))))))
			commands += condition.accept(this);
			commands += "ifeq " + elseLabel + "\n";
		}

		commands += "goto " + endLabel + "\n";
		commands += elseLabel + ":\n";



		for (var statement : ifStatement.getThenBody()) {
			// commands += statement.accept(this);
			
			String temp = statement.accept(this); // FIXME: this is a temporary fix till we implement the rest of the methods
			if (temp != null) {
				commands += temp;
			}
		}

		commands += "goto " + endLabel + "\n";
		commands += elseLabel + ":\n";

		for (var statement : ifStatement.getElseBody()) {
			// commands += statement.accept(this);
			
			String temp = statement.accept(this); // FIXME: this is a temporary fix till we implement the rest of the methods
			if (temp != null) {
				commands += temp;
			}
		}

		commands += endLabel + ":\n";
		return commands;
	}

	@Override
	public String visit(PutStatement putStatement) {
		String commands = "";
		commands += "getstatic java/lang/System/out Ljava/io/PrintStream;\n";
		commands += putStatement.getExpression().accept(this);
		commands += "invokevirtual java/io/PrintStream/println(" + 
					getTypeSignature(putStatement.getExpression().accept(typeChecker)) + ")V\n";
		return commands;
	}

	@Override
	public String visit(ReturnStatement returnStatement) {
		// since we determined the return type in function declaration, we can just visit the expression and nothing else
		if (returnStatement.hasRetExpression()) {
			return returnStatement.getReturnExp().accept(this);
		}
		return null;
	}

	@Override
	public String visit(ExpressionStatement expressionStatement) {
		return expressionStatement.getExpression().accept(this);
	}

	@Override
	public String visit(BinaryExpression binaryExpression) {
		String commands = "";
		commands += binaryExpression.getFirstOperand().accept(this);
		commands += binaryExpression.getSecondOperand().accept(this);
		BinaryOperator operator = binaryExpression.getOperator();
		switch (operator) {
			// we only have int and bool types so we don't need to check
			case PLUS		        -> commands += "iadd\n";
			case MINUS		        -> commands += "isub\n";
			case MULT		        -> commands += "imul\n";
			case DIVIDE		        -> commands += "idiv\n";
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
		switch (unaryExpression.getOperator()) {
			case MINUS         -> commands += "ineg\n";
			case NOT           -> commands += "iconst_1\nixor\n";
			case null, default -> {}
		}
		return commands;
	}

	@Override
	public String visit(Identifier identifier) {
		String varName = identifier.getName();
		int slot = slotOf(varName);
		Type varType = identifier.accept(typeChecker);
		String commands = "";
		if (varType instanceof IntType || varType instanceof BoolType) {
			commands += "iload " + slot + "\n";
		} else if (varType instanceof StringType) {
			commands += "aload " + slot + "\n";
		} else if (varType instanceof FptrType) {
			// TODO: dunno what to do with this yet
		} else if (varType instanceof ListType) {
			// TODO: dunno what to do with this yet
		} else {
			commands += "aload " + slot + "\n";
		}
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
		for (var statement : loopDoStatement.getLoopBodyStmts()) {
			// commands += statement.accept(this);
			
			String temp = statement.accept(this); // FIXME: this is a temporary fix till we implement the rest of the methods
			if (temp != null) {
				commands += temp;
			}
		}

		commands += endLabel + ":\n";
		commands += "goto " + startLabel + "\n";
		breakLabels.pop();
		nextLabels.pop();
		

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
		} else if (lenType instanceof ListType) { // not sure if this is right, i still dunno how to handle lists
			commands += "invokevirtual List/size()I\n";
		} 

		return commands;
	}

	@Override
	public String visit(ChopStatement chopStatement) {
		String commands = "";
		commands += chopStatement.getChopExpression().accept(this);
		commands += "iconst_1\n";
		commands += "invokevirtual java/lang/String/substring(I)Ljava/lang/String;\n";
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
		// Fuck you
		return null;
	}

	@Override
	public String visit(IntValue intValue) {
		//TODO, use "invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer" to convert to primitive
		// why? why should we store it with class?
		String commands = "";
		commands += "ldc " + intValue.getIntVal();
		return commands;
	}
		
	@Override
	public String visit(BoolValue boolValue) {
		//TODO, use "invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean" to convert to primitive
		String commands = "";
		commands += "ldc " + boolValue.getIntValue();
		return commands;
	}

	@Override
	public String visit(StringValue stringValue) {
		//TODO
		String commands = "";
		commands += stringValue.getStrWithQuotes();
		return commands;
	}
}
