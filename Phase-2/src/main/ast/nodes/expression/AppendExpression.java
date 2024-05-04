package main.ast.nodes.expression;

import java.util.ArrayList;
import main.visitor.IVisitor;

public class AppendExpression extends Expression {

	private Expression appendee;
	private ArrayList<Expression> appendeds = new ArrayList<>();

	public AppendExpression(Expression appendee, ArrayList<Expression> appendeds) {
		this.appendee = appendee;
		this.appendeds = appendeds;
	}

	public AppendExpression(Expression appendee, Expression appended) {
		this.appendee = appendee;
		this.appendeds.add(appended);
	}

	public AppendExpression(Expression appendee) {
		this.appendee = appendee;
	}

	public void addAppendedExpression(Expression expression) {
		this.appendeds.add(expression);
	}

	public Expression getAppendee() {
		return this.appendee;
	}

	public ArrayList<Expression> getAppendeds() {
		return this.appendeds;
	}

	public void setAppendee(Expression appendee) {
		this.appendee = appendee;
	}

	public void setAppendeds(ArrayList<Expression> appendeds) {
		this.appendeds = appendeds;
	}

	@Override
	public String toString() {
		return "AppendExpression";
	}

	@Override
	public <T> T accept(IVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
