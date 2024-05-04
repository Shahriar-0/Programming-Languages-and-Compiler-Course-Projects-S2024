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
import main.visitor.nameAnalyzer.Utility;

public class NameAnalyzer extends Visitor<Void> {

	public ArrayList<CompileError> nameErrors = new ArrayList<>();
	private Utility utility = new Utility();

	@Override
	public Void visit(Program program) {
		SymbolTable.root = new SymbolTable();
		SymbolTable.top = new SymbolTable();

		ArrayList<FunctionItem> functionItems = utility.getFunctionItems(program, this);
		ArrayList<PatternItem> patternItems = utility.getPatternItems(program, this);

		utility.visitFunctions(program, functionItems, this);
		utility.visitPatterns(program, patternItems, this);
		utility.visitMain(program, this);

		return null;
	}
	//TODO:visit all other AST nodes and find name errors

	
}
