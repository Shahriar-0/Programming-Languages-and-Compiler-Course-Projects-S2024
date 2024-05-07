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
    public void visitFunctions(Program program, NameAnalyzer nameAnalyzer) {
		for (FunctionDeclaration functionDeclaration : program.getFunctionDeclarations()) {
			functionDeclaration.accept(nameAnalyzer);
		}
	}

	public void visitPatterns(Program program, NameAnalyzer nameAnalyzer) {
		for (PatternDeclaration patternDeclaration : program.getPatternDeclarations()) {
			patternDeclaration.accept(nameAnalyzer);
		}
	}

    public void visitMain(Program program, NameAnalyzer nameAnalyzer) {
        program.getMain().accept(nameAnalyzer);
    }

	public void checkPatternNames(Program program, NameAnalyzer nameAnalyzer) {
		int duplicatePatternId = 0;

		for (PatternDeclaration patternDeclaration : program.getPatternDeclarations()) {
			PatternItem patternItem = new PatternItem(patternDeclaration);
			try {
				SymbolTable.root.put(patternItem);
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

				try {
					SymbolTable.root.put(newItem);
				} catch (ItemAlreadyExists ignored) {}
			}
		}
		return;
	}

	public void checkFunctionNames(Program program, NameAnalyzer nameAnalyzer) {
		int duplicateFunctionId = 0;
		for (FunctionDeclaration functionDeclaration : program.getFunctionDeclarations()) {
			FunctionItem functionItem = new FunctionItem(functionDeclaration);
			try {
				SymbolTable.root.put(functionItem);

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

				try {
					SymbolTable.root.put(newItem);
				} catch (ItemAlreadyExists ignored) {}
			}
		}

		return;
	}
}
