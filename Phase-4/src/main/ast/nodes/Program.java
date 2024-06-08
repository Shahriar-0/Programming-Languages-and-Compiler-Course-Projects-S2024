package main.ast.nodes;

import java.util.ArrayList;
import main.ast.nodes.declaration.FunctionDeclaration;
import main.ast.nodes.declaration.MainDeclaration;
import main.ast.nodes.declaration.PatternDeclaration;
import main.visitor.IVisitor;

public class Program extends Node {

	private ArrayList<FunctionDeclaration> functionDeclarations = new ArrayList<>();
	private ArrayList<PatternDeclaration> patternDeclarations = new ArrayList<>();
	private MainDeclaration main = new MainDeclaration();

	public ArrayList<FunctionDeclaration> getFunctionDeclarations() {
		return this.functionDeclarations;
	}

	public void setFunctionDeclarations(
		ArrayList<FunctionDeclaration> functionDeclarations
	) {
		this.functionDeclarations = functionDeclarations;
	}

	public void addFunctionDeclaration(
		FunctionDeclaration functionDeclaration
	) {
		this.functionDeclarations.add(functionDeclaration);
	}

	public ArrayList<PatternDeclaration> getPatternDeclarations() {
		return this.patternDeclarations;
	}

	public void setPatternDeclarations(
		ArrayList<PatternDeclaration> patternDeclarations
	) {
		this.patternDeclarations = patternDeclarations;
	}

	public void addPatternDeclaration(PatternDeclaration patternDeclaration) {
		this.patternDeclarations.add(patternDeclaration);
	}

	public MainDeclaration getMain() {
		return this.main;
	}

	public void setMain(MainDeclaration mainDeclaration) {
		this.main = mainDeclaration;
	}

	@Override
	public String toString() {
		return "FLProgram";
	}

	@Override
	public <T> T accept(IVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
