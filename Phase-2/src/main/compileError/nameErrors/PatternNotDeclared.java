package main.compileError.nameErrors;

import main.compileError.CompileError;

public class PatternNotDeclared extends CompileError {

	private String name;

	public PatternNotDeclared(int line, String name) {
		this.line = line;
		this.name = name;
	}

	public String getErrorMessage() {
		return (
			"Line:" + this.line + "-> pattern " + this.name + " is not declared"
		);
	}
}
