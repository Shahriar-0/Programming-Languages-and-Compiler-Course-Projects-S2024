package main.visitor.type;

import java.util.*;
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
			for (int i = 0; i < functionDeclaration.getArgs().size(); i++) {
				VarItem argItem = new VarItem(
					functionDeclaration.getArgs().get(i).getName()
				);
				argItem.setType(currentArgTypes.get(i));
				try {
					SymbolTable.top.put(argItem);
				} catch (ItemAlreadyExists ignored) {}
			}
		} catch (ItemNotFound ignored) {}

		List<Type> returnTypes = new ArrayList<>();
		Type functionReturnType = new NoType();
		boolean hasIncompatibleReturnTypes = false;

		for (Statement statement : functionDeclaration.getBody()) {
			if (statement instanceof ReturnStatement returnStatement) {
				Type returnType = returnStatement.accept(this);

				if (returnTypes.isEmpty()) {
					// this is used instead of `functionReturnType.isNoType()` because that would have made it complicated
					returnTypes.add(returnType);
					functionReturnType = returnType;
				} else {
					if (
						!functionReturnType.sameTypeConsideringNoType(
							returnType
						)
					) {
						hasIncompatibleReturnTypes = true;
						returnTypes.add(returnType); // this is not specifically used, but just in case for debugging
						functionReturnType = new NoType();
					}
				}
			} else {
				statement.accept(this);
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

			VarItem varItem = new VarItem(
				patternDeclaration.getTargetVariable()
			);
			varItem.setType(patternItem.getTargetVarType());
			try {
				SymbolTable.top.put(varItem);
			} catch (ItemAlreadyExists ignored) {}

			for (Expression expression : patternDeclaration.getConditions()) {
				if (!(expression.accept(this) instanceof BoolType)) {
					typeErrors.add(
						new ConditionIsNotBool(expression.getLine())
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
				// also a case here would be when it has default value and we pass something from other type
				FunctionItem functionItem = (FunctionItem) SymbolTable.root.getItem(
					FunctionItem.START_KEY +
					(
						(Identifier) accessExpression.getAccessedExpression()
					).getName()
				);
				ArrayList<Type> argTypes = new ArrayList<>();
				for (Expression arg : accessExpression.getArguments()) {
					argTypes.add(arg.accept(this));
				}
				functionItem.setArgumentTypes(argTypes);
				return functionItem.getFunctionDeclaration().accept(this);
			} catch (ItemNotFound ignored) {}
		} else {
			Type accessedType = accessExpression
				.getAccessedExpression()
				.accept(this);
			if (
				!(accessedType instanceof StringType) &&
				!(accessedType instanceof ListType)
			) {
				typeErrors.add(new IsNotIndexable(accessExpression.getLine()));
				return new NoType();
			} else {
				for (Expression expression : accessExpression.getDimentionalAccess()) {
					if (!(expression.accept(this) instanceof IntType)) {
						typeErrors.add(
							new AccessIndexIsNotInt(expression.getLine())
						);
						return new NoType();
					}
				}
			}
		}
		return null;
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
		forStatement.getRangeExpression().accept(this);
		VarItem varItem = new VarItem(forStatement.getIteratorId());
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
			if (
				!(accessedType instanceof ListType) &&
				!(accessedType instanceof StringType)
			) {
				typeErrors.add(new IsNotIndexable(assignStatement.getLine()));
				return new NoType();
			}

			Expression AccessListExpression = assignStatement.getAccessListExpression();
			if (AccessListExpression != null) {
				if (!(AccessListExpression.accept(this) instanceof IntType)) {
					typeErrors.add(
						new AccessIndexIsNotInt(assignStatement.getLine())
					);
					return new NoType();
				}
			}

			if (accessedType instanceof ListType listType) {
				Type assignExpressionType = assignStatement
					.getAssignExpression()
					.accept(this);
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
			} else { // StringType
				Type assignExpressionType = assignStatement
					.getAssignExpression()
					.accept(this);
				if (!(assignExpressionType instanceof StringType)) {
					typeErrors.add(
						new IsNotIndexable(assignStatement.getLine())
					);
				}
				return new StringType();
			}
		} else if (
			assignStatement.getAssignOperator().equals(AssignOperator.ASSIGN)
		) {
			VarItem newVarItem = new VarItem(assignStatement.getAssignedId());
			Type assignExpressionType = assignStatement
				.getAssignExpression()
				.accept(this);
			newVarItem.setType(assignExpressionType);
			try {
				SymbolTable.top.put(newVarItem);
			} catch (ItemAlreadyExists ignored) {}
			// FIXME: not specified in the document that can we change the type or not
			return assignExpressionType;
		} else { // PLUS_ASSIGN, MINUS_ASSIGN, MULT_ASSIGN, DIV_ASSIGN
			Type assignedType = assignStatement.getAssignedId().accept(this);
			Type assignExpressionType = assignStatement
				.getAssignExpression()
				.accept(this);
			if (
				assignedType instanceof NoType ||
				assignExpressionType instanceof NoType
			) {
				return new NoType();
			} else if (
				assignedType instanceof IntType ||
				assignedType instanceof FloatType
			) {
				if (
					assignExpressionType instanceof IntType ||
					assignExpressionType instanceof FloatType
				) {
					return assignedType;
				} else {
					typeErrors.add(
						new UnsupportedOperandType(
							assignStatement.getLine(),
							assignStatement.getAssignOperator().toString()
						)
					);
					return new NoType();
				}
			} else {
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
		return null;
	}

	@Override
	public Type visit(NextStatement nextStatement) {
		for (Expression expression : nextStatement.getConditions()) {
			if (!(expression.accept(this) instanceof BoolType)) {
				typeErrors.add(new ConditionIsNotBool(expression.getLine()));
			}
		}
		return null;
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
			} else {
				if (!listType.getType().equals(toBeAddedType)) {
					typeErrors.add(
						new PushArgumentsTypesMisMatch(pushStatement.getLine())
					);
				}
			}
		} else if (initialType instanceof StringType) {
			if (!(toBeAddedType instanceof StringType)) {
				typeErrors.add(
					new PushArgumentsTypesMisMatch(pushStatement.getLine())
				);
			}
		} else {
			typeErrors.add(new IsNotPushedable(pushStatement.getLine()));
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
		ArrayList<Type> types = new ArrayList<>();
		for (Expression expression : listValue.getElements()) {
			Type type = expression.accept(this);
			if (type instanceof NoType) {
				return new NoType();
			} else {
				if (types.isEmpty()) {
					types.add(type);
				} else {
					if (!types.get(0).equals(type)) {
						typeErrors.add(
							new ListElementsTypesMisMatch(expression.getLine())
						);
					}
				}
			}
		}
		return null;
	}

	@Override
	public Type visit(FunctionPointer functionPointer) {
		return new FptrType(functionPointer.getId().getName());
	}

	@Override
	public Type visit(AppendExpression appendExpression) {
		Type appendeeType = appendExpression.getAppendee().accept(this);
		if (
			!(appendeeType instanceof ListType) &&
			!(appendeeType instanceof StringType)
		) {
			typeErrors.add(new IsNotAppendable(appendExpression.getLine()));
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

		if (
			firstOperandType instanceof NoType ||
			secondOperandType instanceof NoType
		) {
			return new NoType();
		}

		if (!(firstOperandType.equals(secondOperandType))) {
			typeErrors.add(
				new NonSameOperands(binaryExpression.getLine(), binaryOperator)
			);
			return new NoType();
		} else {
			if (
				binaryOperator.equals(BinaryOperator.EQUAL) ||
				binaryOperator.equals(BinaryOperator.NOT_EQUAL)
			) {
				return new BoolType(); // FIXME: not specified in the document
			} else if (
				binaryOperator.equals(BinaryOperator.GREATER_THAN) ||
				binaryOperator.equals(BinaryOperator.GREATER_EQUAL_THAN) ||
				binaryOperator.equals(BinaryOperator.LESS_THAN) ||
				binaryOperator.equals(BinaryOperator.LESS_EQUAL_THAN)
			) {
				if (
					firstOperandType instanceof IntType ||
					firstOperandType instanceof FloatType
				) {
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
			} else { // PLUS, MINUS, MULT, DIVIDE
				if (
					firstOperandType instanceof IntType ||
					firstOperandType instanceof FloatType
				) {
					return firstOperandType;
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
			return new NoType();
		} else if (unaryOperator.equals(UnaryOperator.NOT)) {
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
		} else { // MINUS, INC, DEC
			if (
				expressionType instanceof IntType ||
				expressionType instanceof FloatType
			) {
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
		if (
			!(
				chompStatement
					.getChompExpression()
					.accept(this) instanceof StringType
			)
		) {
			typeErrors.add(
				new ChompArgumentTypeMisMatch(chompStatement.getLine())
			);
			return new NoType();
		}
		return new StringType();
	}

	@Override
	public Type visit(ChopStatement chopStatement) {
		return new StringType();
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

		if (
			expressionType instanceof StringType ||
			expressionType instanceof ListType
		) {
			return new IntType();
		} else {
			typeErrors.add(new LenArgumentTypeMisMatch(lenStatement.getLine()));
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
			for (Expression expression : rangeExpressions) {
				Type expressionType = expression.accept(this);
				if (expressionType instanceof NoType) {
					return new NoType();
				} else if (rangeTypes.isEmpty()) {
					rangeTypes.add(expressionType);
				} else {
					if (!rangeTypes.get(0).equals(expressionType)) {
						typeErrors.add(
							new ListElementsTypesMisMatch(expression.getLine())
						);
						return new NoType();
					}
				}
			}

			if (rangeTypes.get(0) instanceof IntType) {
				return new ListType(new IntType());
			} else if (rangeTypes.get(0) instanceof FloatType) {
				return new ListType(new FloatType());
			} else {
				typeErrors.add(
					new ListElementsTypesMisMatch(rangeExpression.getLine())
				);
				return new NoType();
			}
		} else if (rangeType.equals(RangeType.DOUBLE_DOT)) {
			Expression rangeExpression1 = rangeExpression
				.getRangeExpressions()
				.get(0);
			Expression rangeExpression2 = rangeExpression
				.getRangeExpressions()
				.get(1);
			Type rangeExpressionType1 = rangeExpression1.accept(this);
			Type rangeExpressionType2 = rangeExpression2.accept(this);

			if (
				rangeExpressionType1 instanceof NoType ||
				rangeExpressionType2 instanceof NoType
			) {
				return new NoType();
			} else if (
				rangeExpressionType1 instanceof IntType &&
				rangeExpressionType2 instanceof IntType
			) {
				return new ListType(new IntType());
			} else {
				typeErrors.add(
					new ListElementsTypesMisMatch(rangeExpression.getLine()) // FIXME: not specified in the document
				);
				return new NoType();
			}
		} else { // identifier
			try {
				VarItem varItem = (VarItem) SymbolTable.top.getItem(
					VarItem.START_KEY +
					(
						(Identifier) rangeExpression
							.getRangeExpressions()
							.get(0)
					).getName()
				);

				if (
					varItem.getType() instanceof ListType ||
					varItem.getType() instanceof StringType
				) {
					return varItem.getType();
				} else {
					typeErrors.add(
						new IsNotIterable(rangeExpression.getLine()) // FIXME: not specified in the document
					);
					return new NoType();
				}
			} catch (ItemNotFound ignored) {
				return new NoType();
			}
		}
	}
}
