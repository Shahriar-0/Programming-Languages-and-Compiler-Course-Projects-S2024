package main.ast.nodes.expression;

import java.util.ArrayList;
import main.ast.nodes.declaration.VarDeclaration;
import main.ast.nodes.statement.Statement;
import main.visitor.IVisitor;

public class LambdaExpression extends Expression {

	ArrayList<VarDeclaration> declarationArgs = new ArrayList<>();
	ArrayList<Statement> body = new ArrayList<>();
	ArrayList<Expression> args = new ArrayList<>();

	public LambdaExpression(ArrayList<VarDeclaration> declarationArgs,ArrayList<Statement> body) {
		this.declarationArgs = declarationArgs;
		this.body = body;
		args = null;
	}

	public LambdaExpression(ArrayList<VarDeclaration> declarationArgs,ArrayList<Statement> body, ArrayList<Expression> args) {
		this.declarationArgs = declarationArgs;
		this.body = body;
		this.args = args;
	}
	
	public ArrayList<VarDeclaration> getDeclarationArgs() {
		return declarationArgs;
	}

	public void setDeclarationArgs(ArrayList<VarDeclaration> declarationArgs) {
		this.declarationArgs = declarationArgs;
	}
	
	public ArrayList<Statement> getBody() {
		return body;
	}

	public void setBody(ArrayList<Statement> body) {
		this.body = body;
	}

	public boolean isCalledImmediately() {
		return args != null;
	}

	public ArrayList<Expression> getArgs() {
		return args;
	}

	public void setArgs(ArrayList<Expression> args) {
		this.args = args;
	}

	public void addArg(Expression arg) {
		args.add(arg);
	}

	public boolean hasValidArgs() {
		int minArgNumber = (int) declarationArgs.stream().filter(arg -> arg.getDefaultVal() == null).count(); 
		int maxArgNumber = declarationArgs.size();
		if (args.size() < minArgNumber || args.size() > maxArgNumber) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "LambdaExpression";
	}

	@Override
	public <T> T accept(IVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
