package main.ast.nodes.statement;

import main.ast.nodes.expression.Expression;
import main.ast.nodes.expression.Identifier;
import main.visitor.IVisitor;

public class AssignStatement extends Statement {

	private boolean accessList;
	private Identifier assignedId;
	private Expression assignExpression;
	private Expression accessListExpression; // for accessing a specific index of an array
	private AssignOperator assignOperator;

	public AssignStatement(
		boolean accessList,
		Identifier assignedId,
		Expression assignExpression,
		AssignOperator assignOperator
	) {
		this.accessList = accessList;
		this.assignedId = assignedId;
		this.assignExpression = assignExpression;
		this.assignOperator = assignOperator;
	}

	public void setAccessList(boolean accessList) {
		this.accessList = accessList;
	}

	public Identifier getAssignedId() {
		return assignedId;
	}

	public void setAssignedId(Identifier assignedId) {
		this.assignedId = assignedId;
	}

	public Expression getAccessListExpression() {
		return accessListExpression;
	}

	public void setAccessListExpression(Expression accessListExpression) {
		this.accessListExpression = accessListExpression;
	}

	public Expression getAssignExpression() {
		return assignExpression;
	}

	public void setAssignExpression(Expression assignExpression) {
		this.assignExpression = assignExpression;
	}

	public AssignOperator getAssignOperator() {
		return assignOperator;
	}

	public void setAssignOperator(AssignOperator assignOperator) {
		this.assignOperator = assignOperator;
	}

	public boolean isAccessList() {
		return accessList;
	}

	@Override
	public String toString() {
		return "AssignTo:" + this.assignedId.toString();
	}

	@Override
	public <T> T accept(IVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
