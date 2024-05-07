package main.symbolTable.item;

import main.ast.nodes.declaration.FunctionDeclaration;
import main.ast.nodes.expression.Identifier;
import main.symbolTable.SymbolTable;

public class FunctionItem extends SymbolTableItem {

	public static final String START_KEY = "Function:";
	private SymbolTable functionSymbolTable;
	private FunctionDeclaration functionDeclaration;

	public FunctionItem(FunctionDeclaration functionDeclaration) {
		this.functionDeclaration = functionDeclaration;
		this.name = this.functionDeclaration.getFunctionName().getName();
	}

	public FunctionItem(Identifier functionName) {
		this.name = functionName.getName();
		this.functionDeclaration = null;
	}

	public SymbolTable getFunctionSymbolTable() {
		return functionSymbolTable;
	}

	public void setFunctionSymbolTable(SymbolTable functionSymbolTable) {
		this.functionSymbolTable = functionSymbolTable;
	}

	public FunctionDeclaration getFunctionDeclaration() {
		return functionDeclaration;
	}

	public void setFunctionDeclaration(FunctionDeclaration functionDeclaration) {
		this.functionDeclaration = functionDeclaration;
	}

	@Override
	public String getKey() {
		return START_KEY + this.name;
	}
}
