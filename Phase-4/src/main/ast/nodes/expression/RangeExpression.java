package main.ast.nodes.expression;

import java.util.ArrayList;
import main.visitor.IVisitor;

public class RangeExpression extends Expression {

	RangeType rangeType;
	ArrayList<Expression> rangeExpressions;

	public RangeExpression(
		RangeType rangeType,
		ArrayList<Expression> rangeExpressions
	) {
		this.rangeExpressions = rangeExpressions;
		this.rangeType = rangeType;
	}

	public void setRangeExpressions(ArrayList<Expression> rangeExpressions) {
		this.rangeExpressions = rangeExpressions;
	}

	public ArrayList<Expression> getRangeExpressions() {
		return rangeExpressions;
	}

	public RangeType getRangeType() {
		return rangeType;
	}

	public void setRangeType(RangeType rangeType) {
		this.rangeType = rangeType;
	}

	@Override
	public String toString() {
		return "RangeExpression:" + rangeType;
	}

	@Override
	public <T> T accept(IVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
