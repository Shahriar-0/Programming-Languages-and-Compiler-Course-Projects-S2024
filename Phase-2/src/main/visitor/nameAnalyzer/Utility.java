package main.visitor.nameAnalyzer;

import java.util.ArrayList;

import main.ast.nodes.Program;
import main.ast.nodes.declaration.FunctionDeclaration;
import main.ast.nodes.declaration.PatternDeclaration;
import main.ast.nodes.expression.Identifier;
import main.compileError.nameErrors.RedefinitionOfFunction;
import main.compileError.nameErrors.RedefinitionOfPattern;
import main.symbolTable.SymbolTable;
import main.symbolTable.exceptions.ItemAlreadyExists;
import main.symbolTable.item.FunctionItem;
import main.symbolTable.item.PatternItem;

public class Utility {
    public void visitFunctions(Program program, ArrayList<FunctionItem> functionItems, NameAnalyzer nameAnalyzer) {
		int visitingFunctionIndex = 0;
		for (FunctionDeclaration functionDeclaration : program.getFunctionDeclarations()) {
			FunctionItem functionItem = functionItems.get(visitingFunctionIndex);
			SymbolTable functionSymbolTable = new SymbolTable();
			functionItem.setFunctionSymbolTable(functionSymbolTable);
			SymbolTable.push(functionSymbolTable);
			functionDeclaration.accept(nameAnalyzer);
			SymbolTable.pop();
			visitingFunctionIndex += 1;
		}
	}

	public void visitPatterns(Program program, ArrayList<PatternItem> patternItems, NameAnalyzer nameAnalyzer) {
		int visitingPatternIndex = 0;
		for (PatternDeclaration patternDeclaration : program.getPatternDeclarations()) {
			PatternItem patternItem = patternItems.get(visitingPatternIndex);
			SymbolTable patternSymbolTable = new SymbolTable();
			patternItem.setPatternSymbolTable(patternSymbolTable);
			SymbolTable.push(patternSymbolTable);
			patternDeclaration.accept(nameAnalyzer);
			SymbolTable.pop();
			visitingPatternIndex += 1;
		}
	}

    public void visitMain(Program program, NameAnalyzer nameAnalyzer) {
        program.getMain().accept(nameAnalyzer);
    }

	public ArrayList<PatternItem> getPatternItems(Program program, NameAnalyzer nameAnalyzer) {
		int duplicatePatternId = 0;
		ArrayList<PatternItem> patternItems = new ArrayList<>();
		for (PatternDeclaration patternDeclaration : program.getPatternDeclarations()) {
			PatternItem patternItem = new PatternItem(patternDeclaration);
			try {
				SymbolTable.root.put(patternItem);
				patternItems.add(patternItem);
			} catch (ItemAlreadyExists e) {
                nameAnalyzer.nameErrors.add(
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

	public ArrayList<FunctionItem> getFunctionItems(Program program, NameAnalyzer nameAnalyzer) {
		int duplicateFunctionId = 0;
		ArrayList<FunctionItem> functionItems = new ArrayList<>();
		for (FunctionDeclaration functionDeclaration : program.getFunctionDeclarations()) {
			FunctionItem functionItem = new FunctionItem(functionDeclaration);
			try {
				SymbolTable.root.put(functionItem);
				functionItems.add(functionItem);
			} catch (ItemAlreadyExists e) {
				nameAnalyzer.nameErrors.add(
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
