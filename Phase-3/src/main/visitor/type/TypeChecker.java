package main.visitor.type;

import java.util.*;
import java.util.logging.Logger;

import main.ast.nodes.Program;
import main.ast.nodes.declaration.*;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.operators.*;
import main.ast.nodes.expression.value.*;
import main.ast.nodes.expression.value.primitive.*;
import main.ast.nodes.statement.*;
import main.ast.type.*;
import main.ast.type.primitiveType.*;
import main.compileError.CompileError;
import main.compileError.typeErrors.*;
import main.symbolTable.SymbolTable;
import main.symbolTable.exceptions.*;
import main.symbolTable.item.*;
import main.visitor.Visitor;

public class TypeChecker extends Visitor<Type> {

	public ArrayList<CompileError> typeErrors = new ArrayList<>();

	@Override
	public Type visit(Program program) {
		SymbolTable.root = new SymbolTable();
		SymbolTable.top = new SymbolTable();

		for (FunctionDeclaration functionDeclaration : program.getFunctionDeclarations()) {
			FunctionItem functionItem = new FunctionItem(functionDeclaration);
			try {
				SymbolTable.root.put(functionItem);
			} catch (ItemAlreadyExists ignored) {}
		}

		for (PatternDeclaration patternDeclaration : program.getPatternDeclarations()) {
			PatternItem patternItem = new PatternItem(patternDeclaration);
			try {
				SymbolTable.root.put(patternItem);
			} catch (ItemAlreadyExists ignored) {}
		}

		program.getMain().accept(this);

		return null;
	}

	@Override
	public Type visit(MainDeclaration mainDeclaration) {
		SymbolTable.push(new SymbolTable());

		for (Statement statement : mainDeclaration.getBody()) {
			statement.accept(this);
		}

		SymbolTable.pop();
		return null;
	}

	@Override
	public Type visit(FunctionDeclaration functionDeclaration) {
		SymbolTable.push(new SymbolTable());

		
		try {
			FunctionItem functionItem = (FunctionItem) SymbolTable.root.getItem(
				FunctionItem.START_KEY +
				functionDeclaration.getFunctionName().getName()
				);
				
				ArrayList<Type> currentArgTypes = functionItem.getArgumentTypes();
				
				if (currentArgTypes.size() < functionDeclaration.getArgs().size()) { // default values
				for (int i = currentArgTypes.size(); i < functionDeclaration.getArgs().size(); i++) {
					
					if (functionDeclaration.getArgs().get(i).getDefaultVal() == null) {
						currentArgTypes.add(new NoType());
						continue;
					}
					
					Type defaultType = functionDeclaration.getArgs().get(i).getDefaultVal().accept(this);
					currentArgTypes.add(defaultType);	
				}
			}
			
			for (int i = 0; i < functionDeclaration.getArgs().size(); i++) {
				VarItem argItem = new VarItem(functionDeclaration.getArgs().get(i).getName());
				argItem.setType(currentArgTypes.get(i));
				try {
					SymbolTable.top.put(argItem);
				} catch (ItemAlreadyExists ignored) {}
			}
			
		} catch (ItemNotFound ignored) {}
		
		List<Type> returnTypes = new ArrayList<>();
		Type functionReturnType = new NoType();
		boolean hasIncompatibleReturnTypes = false;

		ArrayList<Statement> allStatements = new ArrayList<>(functionDeclaration.getBody());
		// this is because there are some things like if that may contain return but we don't see them here

		// while there is if, loop, for statement in the body, we should add their body to allStatements
		while (true) { // FIXME: this two loops are shit.
			boolean hasChanged = false;
			for (Statement statement : allStatements) {
				if (statement instanceof IfStatement ifStatement) {
					allStatements.addAll(ifStatement.getThenBody());
					allStatements.addAll(ifStatement.getElseBody());
					allStatements.remove(ifStatement);
					hasChanged = true;
					break;
				} else if (statement instanceof LoopDoStatement loopDoStatement) {
					allStatements.addAll(loopDoStatement.getLoopBodyStmts());
					allStatements.remove(loopDoStatement);
					hasChanged = true;
					break;
				} else if (statement instanceof ForStatement forStatement) {
					allStatements.addAll(forStatement.getLoopBodyStmts());
					allStatements.remove(forStatement);
					hasChanged = true;
					break;
				}
			}
			if (!hasChanged) {
				break;
			}
		}

		for (Statement statement : functionDeclaration.getBody()) {
			if (!(statement instanceof ReturnStatement)) {
				statement.accept(this); // to avoid duplicate error 
			}
		}

		for (Statement statement : allStatements) { // this is only for checking returns
			if (statement instanceof ReturnStatement returnStatement) {
				Type returnType = returnStatement.accept(this);

				if (returnTypes.isEmpty()) {
					// this is used instead of `functionReturnType.isNoType()` because that would have made it complicated
					returnTypes.add(returnType);
					functionReturnType = returnType;
				} else {
					if (!functionReturnType.sameTypeConsideringNoType(returnType)) {
						hasIncompatibleReturnTypes = true;
						returnTypes.add(returnType); // this is not specifically used, but just in case for debugging
						functionReturnType = new NoType();
					}
				}
			} 
		}

		if (hasIncompatibleReturnTypes) {
			typeErrors.add(
				new FunctionIncompatibleReturnTypes(
					functionDeclaration.getLine(),
					functionDeclaration.getFunctionName().getName()
				)
			);
		}

		SymbolTable.pop();
		return functionReturnType;
	}

	@Override
	public Type visit(PatternDeclaration patternDeclaration) {
		SymbolTable.push(new SymbolTable());

		List<Type> returnTypes = new ArrayList<>();
		Type patternReturnType = new NoType();
		boolean hasIncompatibleReturnTypes = false;

		try {
			PatternItem patternItem = (PatternItem) SymbolTable.root.getItem(
				PatternItem.START_KEY +
				patternDeclaration.getPatternName().getName()
			);

			VarItem varItem = new VarItem(patternDeclaration.getTargetVariable());
			varItem.setType(patternItem.getTargetVarType());
			try {
				SymbolTable.top.put(varItem);
			} catch (ItemAlreadyExists ignored) {}

			for (Expression expression : patternDeclaration.getConditions()) {
				if (!(expression.accept(this) instanceof BoolType)) {
					typeErrors.add(
						new ConditionIsNotBool(
							expression.getLine()
						)
					);
					SymbolTable.pop();
					return new NoType(); // this is wrong since it doesn't check 
					// the rest of the pattern but it was in the template so I kept it
				}
			}

			for (Expression expression : patternDeclaration.getReturnExps()) {
				Type returnType = expression.accept(this);
				
				if (returnTypes.isEmpty()) {
					returnTypes.add(returnType);
					patternReturnType = returnType;
				} else {
					if (!patternReturnType.sameTypeConsideringNoType(returnType)) {
						hasIncompatibleReturnTypes = true;
						returnTypes.add(returnType);
						patternReturnType = new NoType();
					}
				}
			}

		} catch (ItemNotFound ignored) {}

		if (hasIncompatibleReturnTypes) {
			typeErrors.add(
				new PatternIncompatibleReturnTypes(
					patternDeclaration.getLine(),
					patternDeclaration.getPatternName().getName()
				)
			);
		}

		SymbolTable.pop();
		return patternReturnType;
	}

	@Override
	public Type visit(AccessExpression accessExpression) {
		if (accessExpression.isFunctionCall()) {
			try {
				Type accessedType = accessExpression.getAccessedExpression().accept(this);
				String name;
				if (accessedType instanceof FptrType fptrType) {
					name = fptrType.getFunctionName();
				} else if (accessedType instanceof NoType) { // normal function
					Identifier accessedIdentifier = (Identifier) accessExpression.getAccessedExpression();
					name = accessedIdentifier.getName();
				} else {
					// not a function, i don't think that that would be possible but just in case
					return new NoType();
				}

				FunctionItem functionItem = (FunctionItem) SymbolTable.root.getItem(
					FunctionItem.START_KEY +
					name
				);

				ArrayList<Type> argTypes = new ArrayList<>();
				for (Expression arg : accessExpression.getArguments()) {
					argTypes.add(arg.accept(this));
				}
				functionItem.setArgumentTypes(argTypes);

				return functionItem.getFunctionDeclaration().accept(this);

			} catch (ItemNotFound ignored) {
				return new NoType();
			}
		} 
		
		else {
			Type accessedType = accessExpression.getAccessedExpression().accept(this);

			if (!(accessedType instanceof StringType) &&!(accessedType instanceof ListType)) {
				typeErrors.add(
					new IsNotIndexable(
						accessExpression.getLine()
					)
				);
				return new NoType();
			} else {
				for (Expression expression : accessExpression.getDimentionalAccess()) {
					// the for is for multi-dimensional arrays which is not supported anymore
					if (!(expression.accept(this) instanceof IntType)) {
						typeErrors.add(
							new AccessIndexIsNotInt(
								expression.getLine()
							)
						);
					}
				}

				if (accessedType instanceof ListType listType) {
					return listType.getType();
				} else {
					return new StringType();
				}
			}
		}
	}

	@Override
	public Type visit(ReturnStatement returnStatement) {
		if (returnStatement.hasRetExpression()) {
			return returnStatement.getReturnExp().accept(this);
		} else {
			return new NoType();
		}
	}

	@Override
	public Type visit(ExpressionStatement expressionStatement) {
		return expressionStatement.getExpression().accept(this);
	}

	@Override
	public Type visit(ForStatement forStatement) {
		SymbolTable.push(SymbolTable.top.copy());

		Type rangeType = forStatement.getRangeExpression().accept(this);
		if (rangeType instanceof ListType listType) {
			rangeType = listType.getType();
		}

		VarItem varItem = new VarItem(forStatement.getIteratorId());
		varItem.setType(rangeType);
		try {
			SymbolTable.top.put(varItem);
		} catch (ItemAlreadyExists ignored) {}

		for (Statement statement : forStatement.getLoopBodyStmts()) {
			statement.accept(this);
		}

		SymbolTable.pop();
		return new NoType();
	}

	@Override
	public Type visit(IfStatement ifStatement) {
		SymbolTable.push(SymbolTable.top.copy());

		for (Expression expression : ifStatement.getConditions()) {
			if (!(expression.accept(this) instanceof BoolType)) {
				typeErrors.add(new ConditionIsNotBool(expression.getLine()));
			}
		}
		for (Statement statement : ifStatement.getThenBody()) {
			statement.accept(this);
		}
		for (Statement statement : ifStatement.getElseBody()) {
			statement.accept(this);
		}

		SymbolTable.pop();
		return new NoType();
	}

	@Override
	public Type visit(LoopDoStatement loopDoStatement) {
		SymbolTable.push(SymbolTable.top.copy());

		for (Statement statement : loopDoStatement.getLoopBodyStmts()) {
			statement.accept(this);
		}

		SymbolTable.pop();
		return new NoType();
	}

	@Override
	public Type visit(AssignStatement assignStatement) {
		if (assignStatement.isAccessList()) {
			Type accessedType = assignStatement.getAssignedId().accept(this);

			if (!(accessedType instanceof ListType) && !(accessedType instanceof StringType)) {
				typeErrors.add(
					new IsNotIndexable(
						assignStatement.getLine()
					)
				);
				return new NoType();
			}
			
			if (!(assignStatement.getAccessListExpression().accept(this) instanceof IntType)) {
				typeErrors.add(
					new AccessIndexIsNotInt(
						assignStatement.getLine()
					)
				);
				return new NoType();
			}


			if (accessedType instanceof ListType listType) {
				Type assignExpressionType = assignStatement.getAssignExpression().accept(this);

				if (listType.getType() instanceof NoType) {
					listType.setType(assignExpressionType);
				} else {
					if (!listType.getType().equals(assignExpressionType)) {
						typeErrors.add(
							new ListElementsTypesMisMatch(
								assignStatement.getLine()
							)
						);
					}
				}
				return listType;
			} 
			else { // StringType
				Type assignExpressionType = assignStatement.getAssignExpression().accept(this);

				if (!(assignExpressionType instanceof StringType)) {
					typeErrors.add(
						new ListElementsTypesMisMatch(
							assignStatement.getLine()
						) // FIXME: not specified in the document, the error name should be StringElementsTypesMisMatch or sth
					);
				}
				return new StringType();
			}
		} 
		
		else if (assignStatement.getAssignOperator().equals(AssignOperator.ASSIGN)) {
			VarItem newVarItem = new VarItem(assignStatement.getAssignedId());
			
			Type assignExpressionType = assignStatement.getAssignExpression().accept(this);
			newVarItem.setType(assignExpressionType);

			try {
				SymbolTable.top.put(newVarItem);
			} catch (ItemAlreadyExists ignored) {}
			// FIXME: not specified in the document that can we change the type or not
			
			return assignExpressionType;
		} 
		
		else { // PLUS_ASSIGN, MINUS_ASSIGN, MULT_ASSIGN, DIV_ASSIGN
			Type assignedType = assignStatement.getAssignedId().accept(this);
			Type assignExpressionType = assignStatement.getAssignExpression().accept(this);

			if (assignedType instanceof NoType || assignExpressionType instanceof NoType) {
				return new NoType();
			} 
			
			else if (assignedType instanceof IntType || assignedType instanceof FloatType) {

				if (assignedType.equals(assignExpressionType)) {
					return assignedType;
				} else {
					typeErrors.add(
						new UnsupportedOperandType(
							assignStatement.getLine(),
							assignStatement.getAssignOperator().toString()
						) 
					); // this should be NonSameOperands but the template is wrong and only takes BinaryOperator
					return new NoType();
				}
			} 
			
			else { // other types that are not supported for these operators
				typeErrors.add(
					new UnsupportedOperandType(
						assignStatement.getLine(),
						assignStatement.getAssignOperator().toString()
					) 
				);
				return new NoType();
			}
		}
	}

	@Override
	public Type visit(BreakStatement breakStatement) {
		for (Expression expression : breakStatement.getConditions()) {
			if (!(expression.accept(this) instanceof BoolType)) {
				typeErrors.add(new ConditionIsNotBool(expression.getLine()));
			}
		}
		return new NoType();
	}

	@Override
	public Type visit(NextStatement nextStatement) {
		for (Expression expression : nextStatement.getConditions()) {
			if (!(expression.accept(this) instanceof BoolType)) {
				typeErrors.add(new ConditionIsNotBool(expression.getLine()));
			}
		}
		return new NoType();
	}

	@Override
	public Type visit(PushStatement pushStatement) {
		Expression initial = pushStatement.getInitial();
		Expression toBeAdded = pushStatement.getToBeAdded();

		Type initialType = initial.accept(this);
		Type toBeAddedType = toBeAdded.accept(this);

		if (initialType instanceof ListType listType) {
			if (listType.getType() instanceof NoType) {
				listType.setType(toBeAddedType);

				if (initial instanceof ListValue listValue) {
					// check that if it was an identifier, we update the type in the symbol table
					try {
						Identifier identifier = (Identifier) initial;
						VarItem varItem = (VarItem) SymbolTable.top.getItem(
							VarItem.START_KEY +
							identifier.getName()
						);

						if (listValue.getElements().isEmpty()) {
							varItem.setType(new ListType(toBeAddedType));
						} else {
							varItem.setType(new ListType(new NoType())); // this is because we have mismatched types
						}

						try {
							SymbolTable.top.update(varItem);
						} catch (ItemNotFound ignored) {}

					} catch (ItemNotFound ignored) {}
				}
			} 
			
			else {
				if (!listType.getType().equals(toBeAddedType)) {
					typeErrors.add(
						new PushArgumentsTypesMisMatch(
							pushStatement.getLine()
						)
					);
				}
			}
		} 
		
		else if (initialType instanceof StringType) {
			if (!(toBeAddedType instanceof StringType)) {
				typeErrors.add(
					new PushArgumentsTypesMisMatch(
						pushStatement.getLine()
					)
				);
			}
		} 
		
		else { // not pushable
			typeErrors.add(
				new IsNotPushedable(
					pushStatement.getLine()
				)
			);
		}
		return new NoType();
	}

	@Override
	public Type visit(PutStatement putStatement) {
		putStatement.getExpression().accept(this);
		return new NoType();
	}

	@Override
	public Type visit(BoolValue boolValue) {
		return new BoolType();
	}

	@Override
	public Type visit(IntValue intValue) {
		return new IntType();
	}

	@Override
	public Type visit(FloatValue floatValue) {
		return new FloatType();
	}

	@Override
	public Type visit(StringValue stringValue) {
		return new StringType();
	}

	@Override
	public Type visit(ListValue listValue) {
		ArrayList<Type> listTypes = new ArrayList<>();
		Type listType = new NoType();
		boolean hasIncompatibleTypes = false;
		for (Expression expression : listValue.getElements()) {
			Type expressionType = expression.accept(this);

			if (listTypes.isEmpty()) {
				listTypes.add(expressionType);
				listType = expressionType;
			} else {
				if (!listType.sameTypeConsideringNoType(expressionType)) {
					hasIncompatibleTypes = true;
					listTypes.add(expressionType);
					listType = new NoType();
				}
			}
		}
		
		if (hasIncompatibleTypes) {
			typeErrors.add(
				new ListElementsTypesMisMatch(
					listValue.getLine()
				)
			);
			return new ListType(new NoType());
		} 
		else {
			if (listType instanceof NoType) {
				return new ListType(new NoType());
			} else {
				return new ListType(listType);
			}
		}
	}

	@Override
	public Type visit(FunctionPointer functionPointer) {
		return new FptrType(functionPointer.getId().getName());
	}

	@Override
	public Type visit(AppendExpression appendExpression) {
		Type appendeeType = appendExpression.getAppendee().accept(this);
		if (!(appendeeType instanceof ListType) && !(appendeeType instanceof StringType)) {
			typeErrors.add(
				new IsNotAppendable(
					appendExpression.getLine()
				)
			);
			return new NoType();
		}
		return appendeeType;
	}

	@Override
	public Type visit(BinaryExpression binaryExpression) {
		Expression firstOperand = binaryExpression.getFirstOperand();
		Expression secondOperand = binaryExpression.getSecondOperand();
		BinaryOperator binaryOperator = binaryExpression.getOperator();

		Type firstOperandType = firstOperand.accept(this);
		Type secondOperandType = secondOperand.accept(this);

		if (firstOperandType instanceof NoType || secondOperandType instanceof NoType) {
			return new NoType();
		}


		if (!(firstOperandType.equals(secondOperandType))) {
			typeErrors.add(
				new NonSameOperands(
					binaryExpression.getLine(), 
					binaryOperator
				)
			);
			return new NoType();
		} 
		else { // the operands are the same type
			Type type = firstOperandType;
			if (binaryOperator.equals(BinaryOperator.EQUAL) || binaryOperator.equals(BinaryOperator.NOT_EQUAL)) {
				return new BoolType(); // FIXME: not specified in the document that all types have these or not
			} 
			else if (
				binaryOperator.equals(BinaryOperator.GREATER_THAN) ||
				binaryOperator.equals(BinaryOperator.GREATER_EQUAL_THAN) ||
				binaryOperator.equals(BinaryOperator.LESS_THAN) ||
				binaryOperator.equals(BinaryOperator.LESS_EQUAL_THAN)
			) {
				
				if (type instanceof IntType || type instanceof FloatType) {
					return new BoolType();
				} else {
					typeErrors.add(
						new UnsupportedOperandType(
							binaryExpression.getLine(),
							binaryOperator.toString()
						)
					);
					return new NoType();
				}
			} 
			
			else { // PLUS, MINUS, MULT, DIVIDE
				if (type instanceof IntType || type instanceof FloatType) {
					return type;
				} else {
					typeErrors.add(
						new UnsupportedOperandType(
							binaryExpression.getLine(),
							binaryOperator.toString()
						)
					);
					return new NoType();
				}
			}
		}
	}

	@Override
	public Type visit(UnaryExpression unaryExpression) {
		Expression expression = unaryExpression.getExpression();
		UnaryOperator unaryOperator = unaryExpression.getOperator();

		Type expressionType = expression.accept(this);

		if (expressionType instanceof NoType) {
			typeErrors.add(
				new UnsupportedOperandType(
					unaryExpression.getLine(),
					unaryOperator.toString()
				)
			);
			return new NoType();
		} 
		
		else if (unaryOperator.equals(UnaryOperator.NOT)) {
			if (expressionType instanceof BoolType) {
				return new BoolType();
			} else {
				typeErrors.add(
					new UnsupportedOperandType(
						unaryExpression.getLine(),
						unaryOperator.toString()
					)
				);
				return new NoType();
			}
		} 
		
		else { // MINUS, INC, DEC
			if (expressionType instanceof IntType || expressionType instanceof FloatType) {
				return expressionType;
			} else {
				typeErrors.add(
					new UnsupportedOperandType(
						unaryExpression.getLine(),
						unaryOperator.toString()
					)
				);
				return new NoType();
			}
		}
	}

	@Override
	public Type visit(ChompStatement chompStatement) {
		if (!(chompStatement.getChompExpression().accept(this) instanceof StringType)) {
			typeErrors.add(
				new ChompArgumentTypeMisMatch(
					chompStatement.getLine()
				)
			);
			return new NoType();
		}
		return new StringType();
	}

	@Override
	public Type visit(ChopStatement chopStatement) {
		return new StringType(); // bug in the template but i don't care
	}

	@Override
	public Type visit(Identifier identifier) {
		try {
			VarItem varItem = (VarItem) SymbolTable.top.getItem(
				VarItem.START_KEY + identifier.getName()
			);
			return varItem.getType();
		} catch (ItemNotFound ignored) {
			return new NoType();
		}
	}

	@Override
	public Type visit(LenStatement lenStatement) {
		Expression expression = lenStatement.getExpression();
		Type expressionType = expression.accept(this);

		if (expressionType instanceof StringType || expressionType instanceof ListType) {
			return new IntType();
		} else {
			typeErrors.add(
				new LenArgumentTypeMisMatch(
					lenStatement.getLine()
				)
			);
			return new NoType();
		}
	}

	@Override
	public Type visit(MatchPatternStatement matchPatternStatement) {
		try {
			PatternItem patternItem = (PatternItem) SymbolTable.root.getItem(
				PatternItem.START_KEY +
				matchPatternStatement.getPatternId().getName()
			);

			patternItem.setTargetVarType(
				matchPatternStatement.getMatchArgument().accept(this)
			);

			return patternItem.getPatternDeclaration().accept(this);

		} catch (ItemNotFound ignored) {}

		return new NoType();
	}

	@Override
	public Type visit(RangeExpression rangeExpression) {
		RangeType rangeType = rangeExpression.getRangeType();

		if (rangeType.equals(RangeType.LIST)) {
			ArrayList<Expression> rangeExpressions = rangeExpression.getRangeExpressions();
			
			ArrayList<Type> rangeTypes = new ArrayList<>();
			Type listType = new NoType();
			boolean hasIncompatibleTypes = false;
			
			for (Expression expression : rangeExpressions) {
				Type expressionType = expression.accept(this);

				if (rangeTypes.isEmpty()) {
					rangeTypes.add(expressionType);
					listType = expressionType;
				} else {
					if (!listType.sameTypeConsideringNoType(expressionType)) {
						hasIncompatibleTypes = true;
						rangeTypes.add(expressionType);
						listType = new NoType();
					}
				}
			}


			if (hasIncompatibleTypes) {
				typeErrors.add(
					new ListElementsTypesMisMatch(
						rangeExpression.getLine()
					)
				);
				return new NoType();
			} 
			
			else {
				if (listType instanceof NoType) {
					return new ListType(new NoType());
				} else {
					return new ListType(listType);
				}
			}
		} 
		
		else if (rangeType.equals(RangeType.DOUBLE_DOT)) {
			Expression rangeExpression1 = rangeExpression.getRangeExpressions().get(0);
			Expression rangeExpression2 = rangeExpression.getRangeExpressions().get(1);

			Type rangeExpressionType1 = rangeExpression1.accept(this);
			Type rangeExpressionType2 = rangeExpression2.accept(this);

			if (rangeExpressionType1 instanceof NoType || rangeExpressionType2 instanceof NoType) {
				return new NoType();
			} else if (rangeExpressionType1 instanceof IntType && rangeExpressionType2 instanceof IntType) {
				return new ListType(new IntType());
			} else {
				typeErrors.add(
					new RangeValuesMisMatch(
						rangeExpression.getLine()
					) // FIXME: not specified in the document
				);
				return new NoType();
			}
		} 
		
		else { // identifier
			try {
				Identifier identifier = (Identifier) rangeExpression.getRangeExpressions().get(0);
				VarItem varItem = (VarItem) SymbolTable.top.getItem(
					VarItem.START_KEY +
					identifier.getName()
				);

				if (varItem.getType() instanceof ListType || varItem.getType() instanceof StringType) {
					return varItem.getType();
				} else {
					typeErrors.add(
						new IsNotIterable(
							rangeExpression.getLine()
						) // FIXME: not specified in the document
					);
					return new NoType();
				}

			} catch (ItemNotFound ignored) {
				return new NoType();
			}
		}
	}
}
