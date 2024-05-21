package main.ast.nodes.statement;

import java.util.ArrayList;
import main.ast.nodes.expression.Expression;
import main.visitor.IVisitor;

public class BreakStatement extends Statement {

	private ArrayList<Expression> conditions = new ArrayList<>();

	public ArrayList<Expression> getConditions() {
		return conditions;
	}

	public void setConditions(ArrayList<Expression> conditions) {
		this.conditions = conditions;
	}

	@Override
	public String toString() {
		return "BreakStatement";
	}

	@Override
	public <T> T accept(IVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
