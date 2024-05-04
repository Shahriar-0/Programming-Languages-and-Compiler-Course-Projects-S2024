package main.ast.nodes.declaration;

import java.util.ArrayList;
import main.ast.nodes.expression.Identifier;
import main.ast.nodes.statement.Statement;
import main.visitor.IVisitor;

public class FunctionDeclaration extends Declaration {

	private Identifier functionName;
	private ArrayList<VarDeclaration> args = new ArrayList<>();
	private ArrayList<Statement> body = new ArrayList<>();
	private int minArgNumber;
	private int maxArgNumber;

	
	public Identifier getFunctionName() {
		return this.functionName;
	}
	
	public void setFunctionName(Identifier functionName) {
		this.functionName = functionName;
	}
	
	public ArrayList<VarDeclaration> getArgs() {
		return this.args;
	}
	
	public void setArgs(ArrayList<VarDeclaration> args) {
		this.args = args;
		maxArgNumber = args.size();
		minArgNumber = (int) args.stream().filter(arg -> arg.getDefaultVal() == null).count();
	}
	
	public void addArg(VarDeclaration arg) {
		this.args.add(arg);
	}
	
	public ArrayList<Statement> getBody() {
		return this.body;
	}
	
	public void setBody(ArrayList<Statement> body) {
		this.body = body;
	}
	
	public void addStmt(Statement stmt) {
		this.body.add(stmt);
	}

	public boolean isArgCountValid(int argCount) {
		return argCount >= minArgNumber && argCount <= maxArgNumber;
	}

	public ArrayList<String> getArgNames() {
		ArrayList<String> argNames = new ArrayList<>();
		for (VarDeclaration arg : args) {
			argNames.add(arg.getName().getName());
		}
		return argNames;
	}
	
	@Override
	public String toString() {
		return "FunctionDeclaration:" + this.functionName.getName();
	}

	@Override
	public <T> T accept(IVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
