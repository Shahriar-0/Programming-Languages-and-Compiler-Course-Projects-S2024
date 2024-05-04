package main.visitor.nameAnalyzer;

import java.util.ArrayList;
import main.ast.nodes.Program;
import main.ast.nodes.declaration.FunctionDeclaration;
import main.ast.nodes.declaration.MainDeclaration;
import main.ast.nodes.declaration.PatternDeclaration;
import main.ast.nodes.declaration.VarDeclaration;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.value.FunctionPointer;
import main.ast.nodes.expression.value.ListValue;
import main.ast.nodes.statement.*;
import main.compileError.CompileError;
import main.compileError.nameErrors.*;
import main.symbolTable.SymbolTable;
import main.symbolTable.exceptions.ItemAlreadyExists;
import main.symbolTable.exceptions.ItemNotFound;
import main.symbolTable.item.FunctionItem;
import main.symbolTable.item.PatternItem;
import main.symbolTable.item.VarItem;
import main.visitor.Visitor;

public class NameAnalyzer extends Visitor<Void> {

	public ArrayList<CompileError> nameErrors = new ArrayList<>();
	private Utility utility = new Utility();

	@Override
	public Void visit(Program program) {
		SymbolTable.root = new SymbolTable();
		SymbolTable.top = new SymbolTable();

		ArrayList<FunctionItem> functionItems = utility.getFunctionItems(
			program,
			this
		);
		ArrayList<PatternItem> patternItems = utility.getPatternItems(
			program,
			this
		);

		utility.visitFunctions(program, functionItems, this);
		utility.visitPatterns(program, patternItems, this);
		utility.visitMain(program, this);

		return null;
	}

	@Override
	public Void visit(Identifier identifier) {
		try {
			String varName = "VAR:" + identifier.getName(); // FIXME: temp solution since weirdly it doesn't work
			SymbolTable.top.getItem(varName);
		} catch (ItemNotFound e) {
			nameErrors.add(
				new VariableNotDeclared(
					identifier.getLine(),
					identifier.getName()
				)
			);
		}
		return null;
	}

	@Override
	public Void visit(VarDeclaration varDeclaration) {
		try {
			SymbolTable.top.put(new VarItem(varDeclaration.getName()));
		} catch (ItemAlreadyExists e) {
			// nameErrors.add(new Redeclaration(varDeclaration.getLine(), varDeclaration.getName()));
			// uncomment the above line if you want to check for redeclaration of variables
		}
		if (varDeclaration.getDefaultVal() != null) {
			varDeclaration.getDefaultVal().accept(this);
		}
		return null;
	}

	@Override
	public Void visit(FunctionDeclaration functionDeclaration) {
		SymbolTable functionSymbolTable = new SymbolTable(SymbolTable.root); // no global variables 
		SymbolTable.push(functionSymbolTable);

		FunctionItem functionItem = new FunctionItem(functionDeclaration);
		functionItem.setFunctionSymbolTable(functionSymbolTable);
		
		String functionName = functionDeclaration.getFunctionName().getName();
		for (String argNames : functionDeclaration.getArgNames()) {
			if (argNames.equals(functionName)) {
				nameErrors.add(
					new IdenticalArgFunctionName(
						functionDeclaration.getLine(),
						functionName
					)
				);
			}
		}

		for (VarDeclaration varDeclaration : functionDeclaration.getArgs()) {
			varDeclaration.accept(this);
		}
		for (Statement statement : functionDeclaration.getBody()) {
			statement.accept(this);
		}
		SymbolTable.pop();
		return null;
	}

	@Override
	public Void visit(PatternDeclaration patternDeclaration) {
		SymbolTable patternSymbolTable = new SymbolTable(SymbolTable.root); // no global variables
		SymbolTable.push(patternSymbolTable);

		PatternItem patternItem = new PatternItem(patternDeclaration);
		patternItem.setPatternSymbolTable(patternSymbolTable);

		if (patternDeclaration.getPatternName().getName().equals(patternDeclaration.getTargetVariable().getName())) {
			nameErrors.add(
				new IdenticalArgPatternName(
					patternDeclaration.getLine(),
					patternDeclaration.getPatternName().getName()
				)
			);
		}

		VarItem targetVarItem = new VarItem(patternDeclaration.getTargetVariable());
		try {
			SymbolTable.top.put(targetVarItem);
		} catch (ItemAlreadyExists e) {
			// nothing to do here, we just add id to symbol table, if it already exists, it's not a problem
		}

		for (Expression expression : patternDeclaration.getConditions()) {
			expression.accept(this);
		}
		for (Expression expression : patternDeclaration.getReturnExp()) {
			expression.accept(this);
		}
		SymbolTable.pop();
		return null;
	}

	@Override
	public Void visit(MainDeclaration mainDeclaration) {
		SymbolTable mainSymbolTable = new SymbolTable(SymbolTable.root); // no global variables
		SymbolTable.push(mainSymbolTable);
		for (Statement statement : mainDeclaration.getBody()) {
			statement.accept(this);
		}
		SymbolTable.pop();
		return null;
	}

	@Override
	public Void visit(ReturnStatement returnStatement) {
		if (returnStatement.hasRetExpression()) {
			returnStatement.getReturnExp().accept(this);
		}
		return null;
	}

	@Override
	public Void visit(IfStatement ifStatement) {
		for (Expression expression : ifStatement.getConditions()) {
			expression.accept(this);
		}
		for (Statement statement : ifStatement.getThenBody()) {
			statement.accept(this);
		}
		for (Statement statement : ifStatement.getElseBody()) {
			statement.accept(this);
		}
		return null;
	}

	@Override
	public Void visit(PutStatement putStatement) {
		// technically, we should check for not enough or too many arguments in the put statement
		// but it's stupidly handled in the parser, so we don't need to check for it here
		putStatement.getExpression().accept(this); 
		return null;
	}

	@Override
	public Void visit(LenStatement lenStatement) {
		lenStatement.getExpression().accept(this);
		return null;
	}

	@Override
	public Void visit(PushStatement pushStatement) {
		pushStatement.getInitial().accept(this);
		pushStatement.getToBeAdded().accept(this);
		return null;
	}

	@Override
	public Void visit(LoopDoStatement loopDoStatement) {
		ArrayList<Statement> loopBodyStmts = loopDoStatement.getLoopBodyStmts();
		ArrayList<Expression> loopConditions = loopDoStatement.getLoopConditions();
		ReturnStatement loopRetStmt = loopDoStatement.getLoopRetStmt();

		SymbolTable loopSymbolTable = new SymbolTable(SymbolTable.top);
		SymbolTable.push(loopSymbolTable);
		for (Statement statement : loopBodyStmts) {
			statement.accept(this);
		}
		for (Expression expression : loopConditions) {
			expression.accept(this);
		}
		if (loopRetStmt != null) {
			loopRetStmt.accept(this);
		}
		return null;
	}

	@Override
	public Void visit(ForStatement forStatement) {
		Identifier iteratorId = forStatement.getIteratorId();
		ArrayList<Expression> rangeExpressions = forStatement.getRangeExpressions();
		ArrayList<Expression> loopBodyExpressions = forStatement.getLoopBodyExpressions();
		ArrayList<Statement> loopBody = forStatement.getLoopBody();
		ReturnStatement returnStatement = forStatement.getReturnStatement();

		SymbolTable forSymbolTable = new SymbolTable(SymbolTable.top);
		SymbolTable.push(forSymbolTable);
		try {
			SymbolTable.top.put(new VarItem(iteratorId));
		} catch (ItemAlreadyExists e) {
			// nameErrors.add(
			// 	new Redeclaration(
			// 		iteratorId.getLine(),
			// 		iteratorId.getName()
			// 	)
			// );
			// uncomment the above line if you want to check for redeclaration of variables
		}
		for (Expression expression : rangeExpressions) {
			expression.accept(this);
		}
		for (Statement statement : loopBody) {
			statement.accept(this);
		}
		for (Expression expression : loopBodyExpressions) {
			expression.accept(this);
		}
		if (returnStatement != null) {
			returnStatement.accept(this);
		}
		SymbolTable.pop();
		return null;
	}

	@Override
	public Void visit(MatchPatternStatement matchPatternStatement) {
		try {
			SymbolTable.root.getItem(matchPatternStatement.getPatternId().getName());
		} catch (ItemNotFound e) {
			nameErrors.add(
				new PatternNotDeclared(
					matchPatternStatement.getLine(),
					matchPatternStatement.getPatternId().getName()
				)
			);
		}
		matchPatternStatement.getMatchArgument().accept(this);
		return null;
	}

	@Override
	public Void visit(ChopStatement chopStatement) {
		chopStatement.getChopExpression().accept(this);
		return null;
	}

	@Override
	public Void visit(ChompStatement chompStatement) {
		chompStatement.getChompExpression().accept(this);
		return null;
	}

	@Override
	public Void visit(AssignStatement assignStatement) {
		boolean isAccessList = assignStatement.isAccessList();
		if (isAccessList) {
			assignStatement.getAccessListExpression().accept(this);
		}
		assignStatement.getAssignExpression().accept(this);
		
		Identifier assignedId = assignStatement.getAssignedId();
		AssignOperator assignOperator = assignStatement.getAssignOperator();

		if (assignOperator != AssignOperator.ASSIGN) {
			try {
				SymbolTable.top.getItem(assignedId.getName());
			} catch (ItemNotFound e) {
				nameErrors.add(
					new VariableNotDeclared(
						assignedId.getLine(),
						assignedId.getName()
					)
				);
			}
		} else {
			try {
				SymbolTable.top.put(new VarItem(assignedId));
			} catch (ItemAlreadyExists e) {
				// nothing to do here, we just add id to list, if it already exists, it's not a problem
			}
		}

		return null;
	}

	@Override
	public Void visit(ExpressionStatement expressionStatement) {
		expressionStatement.getExpression().accept(this);
		return null;
	}

	@Override
	public Void visit(AppendExpression appendExpression) {
		appendExpression.getAppendee().accept(this);
		for (Expression expression : appendExpression.getAppendeds()) {
			expression.accept(this);
		}
		return null;
	}

	@Override
	public Void visit(BinaryExpression binaryExpression) {
		binaryExpression.getFirstOperand().accept(this);
		binaryExpression.getSecondOperand().accept(this);
		return null;
	}

	@Override
	public Void visit(UnaryExpression unaryExpression) {
		unaryExpression.getExpression().accept(this);
		return null;
	}

	@Override
	public Void visit(AccessExpression accessExpression) {
		boolean isFunctionCall = accessExpression.isFunctionCall();
		if (isFunctionCall) {
			try {
				Expression accessedExpression = accessExpression.getAccessedExpression();
				if (accessedExpression instanceof Identifier) {
					Identifier accessedId = (Identifier) accessedExpression;
					String name = "Function:" + accessedId.getName(); // FIXME: temp solution since weirdly it doesn't work
					FunctionItem functionItem = (FunctionItem) SymbolTable.root.getItem(name);
					if (!functionItem.getFunctionDeclaration().isArgCountValid(accessExpression.getArguments().size())) {
						nameErrors.add(
							new ArgMisMatch(
								accessExpression.getLine(),
								accessedId.getName()
							)
						);
					}
				} else {
					throw new ItemNotFound();
				}
			} catch (ItemNotFound e) {
				nameErrors.add(
					new FunctionNotDeclared(
						accessExpression.getLine(),
						accessExpression.getFunctionName()
					)
				);
			}
		} else {
			accessExpression.getAccessedExpression().accept(this);
		}

		for (Expression expression : accessExpression.getDimensionalAccess()) {
			expression.accept(this);
		}
		for (Expression expression : accessExpression.getArguments()) {
			expression.accept(this);
		}
		return null;
	}

	@Override
	public Void visit(LambdaExpression lambdaExpression) {
		SymbolTable lambdaSymbolTable = new SymbolTable(SymbolTable.top);
		SymbolTable.push(lambdaSymbolTable);
		for (VarDeclaration varDeclaration : lambdaExpression.getDeclarationArgs()) {
			varDeclaration.accept(this);
		}
		for (Statement statement : lambdaExpression.getBody()) {
			statement.accept(this);
		}
		SymbolTable.pop();
		return null;
	}

	@Override
	public Void visit(ListValue listValue) {
		for (Expression expression : listValue.getElements()) {
			expression.accept(this);
		}
		return null;
	}

	@Override
	public Void visit(FunctionPointer functionPointer) {
		functionPointer.getId().accept(this);
		return null;
	}

	// boolValue, intValue, stringValue, floatValue -> no need to visit them
}
