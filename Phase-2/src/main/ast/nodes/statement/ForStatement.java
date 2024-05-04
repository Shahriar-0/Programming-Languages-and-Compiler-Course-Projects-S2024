package main.ast.nodes.statement;

import java.util.ArrayList;
import main.ast.nodes.expression.Expression;
import main.ast.nodes.expression.Identifier;
import main.visitor.IVisitor;

public class ForStatement extends Statement {

	private Identifier iteratorId;
	private ArrayList<Expression> rangeExpressions = new ArrayList<>();
	private ArrayList<Expression> loopBodyExpressions = new ArrayList<>();
	private ArrayList<Statement> loopBody = new ArrayList<>();
	private ReturnStatement returnStatement;

	public ForStatement(
		Identifier identifierId,
		ArrayList<Expression> rangeExpressions,
		ArrayList<Expression> loopBodyExpressions,
		ArrayList<Statement> loopBody,
		ReturnStatement returnStatement
	) {
		this.iteratorId = identifierId;
		this.rangeExpressions = rangeExpressions;
		this.loopBodyExpressions = loopBodyExpressions;
		this.loopBody = loopBody;
		this.returnStatement = returnStatement;
	}

	public ArrayList<Expression> getLoopBodyExpressions() {
		return loopBodyExpressions;
	}

	public void setLoopBodyExpressions(ArrayList<Expression> loopBodyExpressions) {
		this.loopBodyExpressions = loopBodyExpressions;
	}

	public ReturnStatement getReturnStatement() {
		return returnStatement;
	}

	public void setReturnStatement(ReturnStatement returnStatement) {
		this.returnStatement = returnStatement;
	}

	public ArrayList<Expression> getRangeExpressions() {
		return rangeExpressions;
	}

	public void setRangeExpressions(ArrayList<Expression> rangeExpressions) {
		this.rangeExpressions = rangeExpressions;
	}

	public ArrayList<Statement> getLoopBody() {
		return loopBody;
	}

	public void setLoopBody(ArrayList<Statement> loopBody) {
		this.loopBody = loopBody;
	}

	public Identifier getIteratorId() {
		return iteratorId;
	}

	public void setIteratorId(Identifier iteratorId) {
		this.iteratorId = iteratorId;
	}

	@Override
	public String toString() {
		return "ForLoop:" + iteratorId.getName();
	}

	@Override
	public <T> T accept(IVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
