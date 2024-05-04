package main.ast.nodes.expression;

import java.util.ArrayList;
import main.visitor.IVisitor;

public class AccessExpression extends Expression {

	//can be either a list, function or lambda expression
	private Expression accessedExpression;
	//can distinguish between list access and function call with this boolean.you can set it in grammar
	private boolean isFunctionCall;
	private String functionName;
	private ArrayList<Expression> arguments = new ArrayList<>();
	private ArrayList<Expression> dimensionalAccess = new ArrayList<>();

	//list or return type of function can be multidimensional list
	public void setIsFunctionCall(boolean isFunctionCall) {
		this.isFunctionCall = isFunctionCall;
	}

	public boolean isFunctionCall() {
		return isFunctionCall;
	}

	public AccessExpression(Expression accessedExpression, ArrayList<Expression> arguments) {
		this.accessedExpression = accessedExpression;
		this.arguments = arguments;
		this.isFunctionCall = false;
	}

	public Expression getAccessedExpression() {
		return accessedExpression;
	}

	public void setAccessedExpression(Expression accessedExpression) {
		this.accessedExpression = accessedExpression;
	}

	public ArrayList<Expression> getDimensionalAccess() {
		return dimensionalAccess;
	}

	public void setDimensionalAccess(ArrayList<Expression> dimensionalAccess) {
		this.dimensionalAccess = dimensionalAccess;
	}
	
	public ArrayList<Expression> getArguments() {
		return arguments;
	}

	public void setArguments(ArrayList<Expression> arguments) {
		this.arguments = arguments;
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	@Override
	public String toString() {
		return "AccessExpression";
	}

	@Override
	public <T> T accept(IVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
