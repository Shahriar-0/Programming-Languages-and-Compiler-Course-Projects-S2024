package main.ast.nodes.declaration;

import java.util.ArrayList;
import main.ast.nodes.expression.Expression;
import main.ast.nodes.expression.Identifier;
import main.visitor.IVisitor;

public class PatternDeclaration extends Declaration {

	private Identifier patternName;
	private Identifier targetVariable;
	private ArrayList<Expression> conditions = new ArrayList<>();
	private ArrayList<Expression> returnExps = new ArrayList<>();

	public PatternDeclaration(Identifier name, Identifier targetVariable) {
		this.patternName = name;
		this.targetVariable = targetVariable;
	}

	public void setPatternName(Identifier name) {
		this.patternName = name;
	}

	public Identifier getPatternName() {
		return this.patternName;
	}

	public void setTargetVariable(Identifier targetVariable) {
		this.targetVariable = targetVariable;
	}

	public Identifier getTargetVariable() {
		return this.targetVariable;
	}

	public ArrayList<Expression> getConditions() {
		return this.conditions;
	}

	public void setConditions(ArrayList<Expression> conditions) {
		this.conditions.addAll((conditions));
	}

	public void addCondition(Expression condition) {
		this.conditions.add(condition);
	}

	public ArrayList<Expression> getReturnExps() {
		return this.returnExps;
	}

	public void setReturnExps(ArrayList<Expression> returnExp) {
		this.returnExps = returnExp;
	}

	public void addReturnExp(Expression returnExp) {
		this.returnExps.add(returnExp);
	}

	@Override
	public String toString() {
		return (
			"PatternDeclaration:" +
			patternName +
			" on variable:" +
			targetVariable.getName()
		);
	}

	@Override
	public <T> T accept(IVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
