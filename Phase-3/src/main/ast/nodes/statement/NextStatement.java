package main.ast.nodes.statement;

import java.util.ArrayList;
import main.ast.nodes.expression.Expression;
import main.visitor.IVisitor;

public class NextStatement extends Statement {

	private ArrayList<Expression> conditions = new ArrayList<>();

	public ArrayList<Expression> getConditions() {
		return conditions;
	}

	public void setConditions(ArrayList<Expression> conditions) {
		this.conditions = conditions;
	}

	@Override
	public String toString() {
		return "NextStatement";
	}

	@Override
	public <T> T accept(IVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
