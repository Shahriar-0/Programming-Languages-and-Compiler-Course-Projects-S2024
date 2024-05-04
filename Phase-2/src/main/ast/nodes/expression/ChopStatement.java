package main.ast.nodes.expression;

import main.visitor.IVisitor;

public class ChopStatement extends Expression {

	private Expression chopExpression;

	public ChopStatement(Expression chopExpression) {
		this.chopExpression = chopExpression;
	}

	public Expression getChopExpression() {
		return this.chopExpression;
	}

	public void setChopExpression(Expression chopExpression) {
		this.chopExpression = chopExpression;
	}

	@Override
	public String toString() {
		return "ChopStatement";
	}

	@Override
	public <T> T accept(IVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
