package main.ast.nodes.statement;

import java.util.ArrayList;
import main.ast.nodes.expression.Expression;
import main.visitor.IVisitor;

public class IfStatement extends Statement {

	// contains all conditions of if and elseIfs and break conditions inside ifBody
	private ArrayList<Expression> conditions = new ArrayList<>();
	private ArrayList<Statement> thenBody = new ArrayList<>();
	private ArrayList<Statement> elseBody = new ArrayList<>();

	public ArrayList<Expression> getConditions() {
		return this.conditions;
	}

	public void addCondition(ArrayList<Expression> condition) {
		this.conditions.addAll(condition);
	}

	public ArrayList<Statement> getThenBody() {
		return this.thenBody;
	}

	public void setThenBody(ArrayList<Statement> thenBody) {
		this.thenBody = thenBody;
	}

	public ArrayList<Statement> getElseBody() {
		return this.elseBody;
	}

	public void setElseBody(ArrayList<Statement> elseBody) {
		this.elseBody = elseBody;
	}

	@Override
	public String toString() {
		return "IfStatement";
	}

	@Override
	public <T> T accept(IVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
