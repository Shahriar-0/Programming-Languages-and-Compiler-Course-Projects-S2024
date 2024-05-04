package main.visitor.nameAnalyzer;

import java.util.ArrayList;
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

public class NameAnalyzer extends Visitor<Void> {

	public ArrayList<CompileError> nameErrors = new ArrayList<>();

	@Override
	public Void visit(Program program) {
		SymbolTable.root = new SymbolTable();
		SymbolTable.top = new SymbolTable();

		//TODO: addFunctions,
		//Code handles duplicate function declarations by renaming and adding them to the symbol table.
		ArrayList<FunctionItem> functionItems = getFunctionItems(program);

		ArrayList<PatternItem> patternItems = getPatternItems(program);
		//TODO:visitFunctions
		//Iterates over function declarations, assigns symbol tables, visits declarations, and manages symbol table stack.

		//visitPatterns
		int visitingPatternIndex = 0;
		for (PatternDeclaration patternDeclaration : program.getPatternDeclarations()) {
			PatternItem patternItem = patternItems.get(visitingPatternIndex);
			SymbolTable patternSymbolTable = new SymbolTable();
			patternItem.setPatternSymbolTable(patternSymbolTable);
			SymbolTable.push(patternSymbolTable);
			patternDeclaration.accept(this);
			SymbolTable.pop();
			visitingPatternIndex += 1;
		}
		//visitMain
		program.getMain().accept(this);
		return null;
	}
	//TODO:visit all other AST nodes and find name errors

	private ArrayList<PatternItem> getPatternItems(Program program) {
		int duplicatePatternId = 0;
		ArrayList<PatternItem> patternItems = new ArrayList<>();
		for (PatternDeclaration patternDeclaration : program.getPatternDeclarations()) {
			PatternItem patternItem = new PatternItem(patternDeclaration);
			try {
				SymbolTable.root.put(patternItem);
				patternItems.add(patternItem);
			} catch (ItemAlreadyExists e) {
				nameErrors.add(
					new RedefinitionOfPattern(
						patternDeclaration.getLine(),
						patternDeclaration.getPatternName().getName()
					)
				);
				duplicatePatternId += 1;
				String freshName =
					patternItem.getName() +
					"#" +
					String.valueOf(duplicatePatternId);
				Identifier newId = patternDeclaration.getPatternName();
				newId.setName(freshName);
				patternDeclaration.setPatternName(newId);
				PatternItem newItem = new PatternItem(patternDeclaration);
				patternItems.add(newItem);
				try {
					SymbolTable.root.put(newItem);
				} catch (ItemAlreadyExists ignored) {}
			}
		}
		return patternItems;
	}

	private ArrayList<FunctionItem> getFunctionItems(Program program) {
		int duplicateFunctionId = 0;
		ArrayList<FunctionItem> functionItems = new ArrayList<>();
		for (FunctionDeclaration functionDeclaration : program.getFunctionDeclarations()) {
			FunctionItem functionItem = new FunctionItem(functionDeclaration);
			try {
				SymbolTable.root.put(functionItem);
				functionItems.add(functionItem);
			} catch (ItemAlreadyExists e) {
				nameErrors.add(
					new RedefinitionOfFunction(
						functionDeclaration.getLine(),
						functionDeclaration.getFunctionName().getName()
					)
				);
				duplicateFunctionId += 1;
				String freshName =
					functionItem.getName() +
					"#" +
					String.valueOf(duplicateFunctionId);
				Identifier newId = functionDeclaration.getFunctionName();
				newId.setName(freshName);
				functionDeclaration.setFunctionName(newId);
				FunctionItem newItem = new FunctionItem(functionDeclaration);
				functionItems.add(newItem);
				try {
					SymbolTable.root.put(newItem);
				} catch (ItemAlreadyExists ignored) {}
			}
		}
		return functionItems;
	}
}
