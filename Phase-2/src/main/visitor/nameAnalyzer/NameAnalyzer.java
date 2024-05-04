package main.visitor.nameAnalyzer;

import java.util.ArrayList;
import java.util.logging.Logger;
import main.ast.nodes.Program;
import main.ast.nodes.declaration.FunctionDeclaration;
import main.ast.nodes.declaration.MainDeclaration;
import main.ast.nodes.declaration.PatternDeclaration;
import main.ast.nodes.declaration.VarDeclaration;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.value.FunctionPointer;
import main.ast.nodes.expression.value.ListValue;
import main.ast.nodes.expression.value.primitive.BoolValue;
import main.ast.nodes.expression.value.primitive.FloatValue;
import main.ast.nodes.expression.value.primitive.IntValue;
import main.ast.nodes.expression.value.primitive.StringValue;
import main.ast.nodes.statement.*;
import main.compileError.CompileError;
import main.compileError.nameErrors.*;
import main.symbolTable.SymbolTable;
import main.symbolTable.exceptions.ItemAlreadyExists;
import main.symbolTable.exceptions.ItemNotFound;
import main.symbolTable.item.FunctionItem;
import main.symbolTable.item.PatternItem;
import main.symbolTable.item.VarItem;
import main.visitor.Visitor;
import main.visitor.nameAnalyzer.Utility;

public class NameAnalyzer extends Visitor<Void> {

	public ArrayList<CompileError> nameErrors = new ArrayList<>();
	private Utility utility = new Utility();

	@Override
	public Void visit(Program program) {
		SymbolTable.root = new SymbolTable();
		SymbolTable.top = new SymbolTable();

		ArrayList<FunctionItem> functionItems = utility.getFunctionItems(
			program,
			this
		);
		ArrayList<PatternItem> patternItems = utility.getPatternItems(
			program,
			this
		);

		utility.visitFunctions(program, functionItems, this);
		utility.visitPatterns(program, patternItems, this);
		utility.visitMain(program, this);

		return null;
	}

	@Override
	public Void visit(Identifier identifier) {
		try {
			SymbolTable.top.getItem(identifier.getName());
		} catch (ItemNotFound e) {
			nameErrors.add(
				new VariableNotDeclared(
					identifier.getLine(),
					identifier.getName()
				)
			);
		}
		return null;
	}

	@Override
	public Void visit(VarDeclaration varDeclaration) {
		try {
			SymbolTable.top.put(new VarItem(varDeclaration.getName()));
		} catch (ItemAlreadyExists e) {
			// nameErrors.add(new Redeclaration(varDeclaration.getLine(), varDeclaration.getName()));
			// uncomment the above line if you want to check for redeclaration of variables
		}
		if (varDeclaration.getDefaultVal() != null) {
			varDeclaration.getDefaultVal().accept(this);
		}
		return null;
	}

	@Override
	public Void visit(FunctionDeclaration functionDeclaration) {
		// the name of the function and it's parameters shouldn't be the same
		// so we add the function to the symbol table before visiting the parameters
		// but we also need to add it as a varItem so that we can check for the parameters
		// with the same name as the function

		// TODO: alternative solutions:
		// 1. add the function to the symbol table after visiting the parameters
		// 2. add the function to the symbol table before visiting the parameters, but don't add it as a varItem,
		//    and instead check for the parameters with the same name as the function in the symbol table

		SymbolTable functionSymbolTable = new SymbolTable();
		SymbolTable.push(functionSymbolTable);

		FunctionItem functionItem = new FunctionItem(functionDeclaration);
		functionItem.setFunctionSymbolTable(functionSymbolTable);

		VarItem functionNameVarItem = new VarItem(functionDeclaration.getFunctionName());
		try {
			SymbolTable.top.put(functionItem);
		} catch (ItemAlreadyExists e) {
			// we already checked for the function name in the previous visitor, so this exception should never be thrown
		}
		for (VarDeclaration varDeclaration : functionDeclaration.getArgs()) {
			varDeclaration.accept(this);
		}
		try {
			SymbolTable.top.put(functionNameVarItem);
		} catch (ItemAlreadyExists e) {
			nameErrors.add(
				new IdenticalArgFunctionName(
					functionDeclaration.getLine(),
					functionDeclaration.getFunctionName().getName()
				)
			);
		}
		for (Statement statement : functionDeclaration.getBody()) {
			statement.accept(this);
		}
		SymbolTable.pop();
		return null;
	}

	@Override
	public Void visit(PatternDeclaration patternDeclaration) {
		// same as the function declaration
		SymbolTable patternSymbolTable = new SymbolTable();
		SymbolTable.push(patternSymbolTable);

		PatternItem patternItem = new PatternItem(patternDeclaration);
		patternItem.setPatternSymbolTable(patternSymbolTable);

		VarItem patternNameVarItem = new VarItem(patternDeclaration.getPatternName());
		try {
			SymbolTable.top.put(patternItem);
			SymbolTable.top.put(patternNameVarItem);
		} catch (ItemAlreadyExists e) {
			// we already checked for the pattern name in the previous visitor, so this exception should never be thrown
		}

		VarItem targetVarItem = new VarItem(patternDeclaration.getTargetVariable());
		try {
			SymbolTable.top.put(targetVarItem);
		} catch (ItemAlreadyExists e) {
			nameErrors.add(
				new IdenticalArgPatternName(
					patternDeclaration.getLine(),
					patternDeclaration.getPatternName().getName()
				)
			);
		}

		for (Expression expression : patternDeclaration.getConditions()) {
			expression.accept(this);
		}
		for (Expression expression : patternDeclaration.getReturnExp()) {
			expression.accept(this);
		}
		SymbolTable.pop();
		return null;
	}
}
