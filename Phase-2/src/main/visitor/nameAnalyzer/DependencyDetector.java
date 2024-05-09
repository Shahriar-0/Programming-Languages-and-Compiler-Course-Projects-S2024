package main.visitor.nameAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import main.ast.nodes.Program;
import main.ast.nodes.declaration.FunctionDeclaration;
import main.ast.nodes.declaration.PatternDeclaration;
import main.ast.nodes.declaration.VarDeclaration;
import main.ast.nodes.expression.AccessExpression;
import main.ast.nodes.expression.AppendExpression;
import main.ast.nodes.expression.BinaryExpression;
import main.ast.nodes.expression.ChompStatement;
import main.ast.nodes.expression.ChopStatement;
import main.ast.nodes.expression.Expression;
import main.ast.nodes.expression.Identifier;
import main.ast.nodes.expression.LambdaExpression;
import main.ast.nodes.expression.LenStatement;
import main.ast.nodes.expression.MatchPatternStatement;
import main.ast.nodes.expression.UnaryExpression;
import main.ast.nodes.expression.value.FunctionPointer;
import main.ast.nodes.expression.value.ListValue;
import main.ast.nodes.statement.AssignStatement;
import main.ast.nodes.statement.ExpressionStatement;
import main.ast.nodes.statement.ForStatement;
import main.ast.nodes.statement.IfStatement;
import main.ast.nodes.statement.LoopDoStatement;
import main.ast.nodes.statement.PushStatement;
import main.ast.nodes.statement.PutStatement;
import main.ast.nodes.statement.ReturnStatement;
import main.ast.nodes.statement.Statement;
import main.compileError.CompileError;
import main.compileError.nameErrors.CircularDependency;
import main.symbolTable.SymbolTable;
import main.symbolTable.exceptions.ItemAlreadyExists;
import main.symbolTable.item.VarItem;
import main.symbolTable.utils.Graph;
import main.visitor.Visitor;

public class DependencyDetector extends Visitor<Void> {

	public ArrayList<CompileError> dependencyError = new ArrayList<>();
	private Graph dependencyGraph = new Graph();
	private Stack<String> callStack = new Stack<>();

	@Override
	public Void visit(Program program) {
		for (FunctionDeclaration functionDeclaration : program.getFunctionDeclarations()) {
			functionDeclaration.accept(this);
		}

		for (PatternDeclaration patternDeclaration : program.getPatternDeclarations()) {
			patternDeclaration.accept(this);
		}
		return null;
	}

	@Override
	public Void visit(VarDeclaration varDeclaration) {
		if (varDeclaration.getDefaultVal() != null) {
			varDeclaration.getDefaultVal().accept(this);
		}
		return null;
	}

	@Override
	public Void visit(FunctionDeclaration functionDeclaration) {
		String functionName = functionDeclaration.getFunctionName().getName();
		callStack.push(functionName);
		for (VarDeclaration varDeclaration : functionDeclaration.getArgs()) {
			varDeclaration.accept(this);
		}
		for (Statement statement : functionDeclaration.getBody()) {
			statement.accept(this);
		}
		callStack.pop();
		return null;
	}

	@Override
	public Void visit(PatternDeclaration patternDeclaration) {
		String patternName = patternDeclaration.getPatternName().getName();
		callStack.push(patternName);
		for (Expression expression : patternDeclaration.getConditions()) {
			expression.accept(this);
		}
		callStack.pop();
		return null;
	}

	@Override
	public Void visit(ReturnStatement returnStatement) {
		Expression returnedExpression = returnStatement.getReturnExp();
		if (returnedExpression != null) {
			returnedExpression.accept(this);
		}
		return null;
	}

	@Override
	public Void visit(IfStatement ifStatement) {
		for (Expression condition : ifStatement.getConditions()) {
			condition.accept(this);
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
		putStatement.getExpression().accept(this);
		return null;
	}

	@Override
	public Void visit(PushStatement pushStatement) {
		pushStatement.getInitial().accept(this);
		pushStatement.getToBeAdded().accept(this);
		return null;
	}

	@Override
	public Void visit(LenStatement lenStatement) {
		lenStatement.getExpression().accept(this);
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
	public Void visit(LoopDoStatement loopDoStatement) {
		ArrayList<Statement> loopBodyStmts = loopDoStatement.getLoopBodyStmts();
		ArrayList<Expression> loopConditions = loopDoStatement.getLoopConditions();
		ReturnStatement loopRetStmt = loopDoStatement.getLoopRetStmt();

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
		ArrayList<Expression> rangeExpressions = forStatement.getRangeExpressions();
		ArrayList<Expression> loopBodyExpressions = forStatement.getLoopBodyExpressions();
		ArrayList<Statement> loopBody = forStatement.getLoopBody();
		ReturnStatement returnStatement = forStatement.getReturnStatement();

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
		return null;
	}

	@Override
	public Void visit(MatchPatternStatement matchPatternStatement) {
		String patternName = matchPatternStatement.getPatternId().getName();
		for (String name : callStack) {
			dependencyGraph.addEdge(name, patternName);
		}
		matchPatternStatement.getMatchArgument().accept(this);
		return null;
	}

	@Override
	public Void visit(AssignStatement assignStatement) {
		assignStatement.getAssignExpression().accept(this);
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
		if (accessExpression.isFunctionCall()) {
			String functionName = accessExpression.getFunctionName();
			for (String name : callStack) {
				dependencyGraph.addEdge(name, functionName);
			}
		}
		for (Expression expression : accessExpression.getArguments()) {
			expression.accept(this);
		}
		for (Expression expression : accessExpression.getDimensionalAccess()) {
			expression.accept(this);
		}
		return null;
	}

	@Override
	public Void visit(LambdaExpression lambdaExpression) {
		for (Statement statement : lambdaExpression.getBody()) {
			statement.accept(this);
		}
		if (lambdaExpression.isCalledImmediately()) {
			for (Expression expression : lambdaExpression.getArgs()) {
				expression.accept(this);
			}
		}
		return null;
	}

	@Override
	public Void visit(ListValue listValue) {
		for (Expression expression : listValue.getElements()) {
			expression.accept(this);
		}
		return null;
	}

	public Void findDependency() {
		ArrayList<List<String>> cycles = dependencyGraph.findCycles();
		for (List<String> cycle : cycles) {
			dependencyError.add(new CircularDependency(cycle));
		}
		return null;
	}
}
