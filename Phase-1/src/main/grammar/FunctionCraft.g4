grammar FunctionCraft;

// ********************************************************************************************************************
// *                                                                                                                  *
// *                                                     Grammar                                                      *
// *                                                                                                                  *
// ********************************************************************************************************************
program
	: (funcDef | pattern)* main eof
	;

funcDef
	: DEF (funcName = IDENTIFIER) {System.out.println("FuncDec: " + $funcName.text);} funcArgs funcBody END
	;

pattern
	: PATTERN (patterName = IDENTIFIER) {System.out.println("PatternDec: " + $patterName.text);} funcArgs patternBody SEMICOLON
	;

main
	: DEF MAIN {System.out.println("MAIN");} LPAR RPAR funcBody END
	;

funcArgs
	: LPAR ((((IDENTIFIER COMMA)* IDENTIFIER) (COMMA defaultArgs)?) | defaultArgs)? RPAR
	;
// case1: a()
// case2: a([])
// case3: a(x1, x2, ...)
// case4: a(x1, x2, ..., [])

defaultArgs
	: LBRACKET (IDENTIFIER ASSIGN expression COMMA)* (IDENTIFIER ASSIGN expression) RBRACKET
	;

patternBody
	: (PATTERNIND condition ASSIGN expression)+
	;

funcBody
	: body return? // body! return for your own sake, the world is not functioning as you wish
	;

return
	: RETURN {System.out.println("RETURN");} expression? SEMICOLON
	;

body
	: (statement)*
	;

nonEmptyBody
	: (statement)+
	;

statement
	: assignment SEMICOLON
	| expression SEMICOLON
	| controlFlow
	| SEMICOLON
	;
// case1: a (= | += | -= | *= | /= | %=) b
// case2: all expressions
// case3: control flow (if, for, loop)
// case4: empty statement (just a semicolon)


// ---------------------- Assignment ----------------------
// ! priority: 12
assignment
	: (name = IDENTIFIER) assigner expression {System.out.println("Assignment: " + $name.text);}
	;

assigner
	: ASSIGN
	| ADDASSIGN
	| DECASSIGN
	| MULTASSIGN
	| DIVASSIGN
	| MODASSIGN
	;
// ---------------------- End Assignment ----------------------


// ---------------------- Control Flows ----------------------
controlFlow
	: if
	| loopDo
	| for
	;

if
	: IF {System.out.println("Decision: IF");} condition body elif else END 
	// NOTE: if the bodies can be empty change the body to nonEmptyBody
	;

elif
	: (ELSEIF {System.out.println("Decision: ELSEIF");} condition body)* 
	;

else
	: (ELSE {System.out.println("Decision: ELSE");} body)? 
	;

loopDo
	: LOOP {System.out.println("Loop: DO");} DO loopBody END
	;

loopBody
	: (statement | loopCondition)*
	;

for
	: FOR {System.out.println("Loop: FOR");} IDENTIFIER IN LPAR rangeGenerator RPAR forBody END
	;

forBody
	: (statement | loopCondition)*
	;

loopCondition
	: NEXT {System.out.println("Control: NEXT");} (IF condition)? SEMICOLON
	| BREAK {System.out.println("Control: BREAK");} (IF condition)? SEMICOLON
	;

rangeGenerator
	: expression RANGE expression
	;

condition
	: par_exp
	;
// ---------------------- End Control Flows ----------------------


// ---------------------- Expressions ----------------------
expression
	: expressionAppend
	;

// ! priority: 11
expressionAppend
	: expressionLogicalOr expressionAppend_
	;

expressionAppend_
	: APPEND expressionLogicalOr { System.out.println("Operator: " + $APPEND.text); } expressionAppend_
	| epsilon
	;

// ! priority: 10
expressionLogicalOr
	: expressionLogicalAnd expressionLogicalOr_
	;

expressionLogicalOr_
	: OR expressionLogicalAnd { System.out.println("Operator: " + $OR.text); } expressionLogicalOr_
	| epsilon
	;

// ! priority: 9
expressionLogicalAnd
	: expressionEqNoteq expressionLogicalAnd_
	;

expressionLogicalAnd_
	: AND expressionEqNoteq { System.out.println("Operator: " + $AND.text); } expressionLogicalAnd_
	| epsilon
	;

// ! priority: 8
expressionEqNoteq
	: expressionCompare expressionEqNoteq_
	;

expressionEqNoteq_
	: EQ expressionCompare { System.out.println("Operator: " + $EQ.text); } expressionEqNoteq_
	| NEQ expressionCompare { System.out.println("Operator: " + $NEQ.text); } expressionEqNoteq_
	| epsilon
	;

// ! priority: 7
expressionCompare
	: expressionAddSub expressionCompare_
	;

expressionCompare_
	: GTR expressionAddSub { System.out.println("Operator: " + $GTR.text); } expressionCompare_
	| GEQ expressionAddSub { System.out.println("Operator: " + $GEQ.text); } expressionCompare_
	| LES expressionAddSub { System.out.println("Operator: " + $LES.text); } expressionCompare_
	| LEQ expressionAddSub { System.out.println("Operator: " + $LEQ.text); } expressionCompare_
	| epsilon
	;

// ! priority: 6
expressionAddSub
	: expressionMultDivMod expressionAddSub_
	;

expressionAddSub_
	: PLUS expressionMultDivMod { System.out.println("Operator: " + $PLUS.text); } expressionAddSub_
	| MINUS expressionMultDivMod { System.out.println("Operator: " + $MINUS.text); } expressionAddSub_
	| epsilon
	;

// ! priority: 5
expressionMultDivMod
	: expressionNotMinus expressionMultDivMod_
	;

expressionMultDivMod_
	: MULT expressionNotMinus { System.out.println("Operator: " + $MULT.text); } expressionMultDivMod_
	| DIV expressionNotMinus { System.out.println("Operator: " + $DIV.text); } expressionMultDivMod_
	| MOD expressionNotMinus { System.out.println("Operator: " + $MOD.text); } expressionMultDivMod_
	| epsilon
	;

// ! priority: 4
expressionNotMinus
	: NOT expressionNotMinus { System.out.println("Operator: " + $NOT.text); }
	| MINUS expressionNotMinus { System.out.println("Operator: " + $MINUS.text); }
	| inPlaceAssignment
	;

// ! priority: 3
inPlaceAssignment
	: value inPlaceAssignment_
	;

inPlaceAssignment_
	: INC { System.out.println("Operator: " + $INC.text); }
	| DEC { System.out.println("Operator: " + $DEC.text); }
	| epsilon
	;

// ! priority: 1, 2
value
	: funcCall
	| primitive
	| IDENTIFIER
	| listDerefrencing
	| method
	| par_exp
	| funcptr
	;

funcptr
	: method
	| lambdaFunc
	;

par_exp
	: LPAR expression RPAR
	;

listDerefrencing
	: (IDENTIFIER | funcCall) (LBRACKET expressionAddSub RBRACKET)+ // no logical
	;

funcCall
	: builtInFunc funcCallArgs_
	| IDENTIFIER funcCallArgs
	| lambdaFunc funcCallArgs_
	// | method funcCallArgs_ 
	// NOTE: if you want to be able to stream a method (i.e. method(:name)(args)) you should uncomment above line
	;

funcCallArgs
	: {System.out.println("FunctionCall");} funcCallArgs_
	// the reason for separating them is that built-in functions shouldn't use the "Function Call" action code
	;

funcCallArgs_
	: LPAR ((expression COMMA)* expression)? RPAR
	;

builtInFunc
	: {System.out.println("Built-In: PUTS");} PUTS
	| {System.out.println("Built-In: LEN");} LEN
	| {System.out.println("Built-In: CHOP");} CHOP
	| {System.out.println("Built-In: CHOMP");} CHOMP
	| {System.out.println("Built-In: PUSH");} PUSH
	| {System.out.println("Built-In: MATCH");} patternMatch
	;

lambdaFunc
	: ARROW {System.out.println("Structure: LAMBDA");} funcArgs LBRACE funcBody RBRACE
	;

method
	: METHOD LPAR COLON IDENTIFIER RPAR
	;

patternMatch
	: IDENTIFIER DOT MATCH
	;

primitive
	: INT_VAL
	| FLOAT_VAL
	| STRING_VAL
	| TRUE
	| FALSE
	| list
	;

list
	: (LBRACKET ((expression COMMA)* expression)? RBRACKET)
	;
// ---------------------- End Expressions ----------------------


// ---------------------- Other ----------------------
eof
	: epsilon
	;

epsilon
	:
	;
// ---------------------- End Other ----------------------


// ********************************************************************************************************************
// *                                                                                                                  *
// *                                                     Tokens                                                       *
// *                                                                                                                  *
// ********************************************************************************************************************

// $antlr-format off

// ---------------------- built-in functions ----------------------
MAIN:         'main';
PATTERN:      'pattern';
PUTS:         'puts';
LEN:          'len';
PUSH:         'push';
CHOP:         'chop';
CHOMP:        'chomp';
MATCH:        'match';
METHOD:       'method';
// ---------------------- end built-in functions ----------------------


// ---------------------- operators ----------------------
PATTERNDELIM: '|';
COMMA:        ',';
SEMICOLON:    ';';
COLON:        ':';
ARROW:        '->';
LPAR:         '(';
RPAR:         ')';
LBRACKET:     '[';
RBRACKET:     ']';
LBRACE:       '{';
RBRACE:       '}';
PLUS:         '+';
MINUS:        '-';
DIV:          '/';
MULT:         '*';
MOD:          '%';
INC:          '++';
DEC:          '--';
RANGE:        '..';
DOT:          '.';
// ---------------------- end operators ----------------------


// ---------------------- types values ----------------------
TRUE:         'true';
FALSE:        'false';
INT_VAL:      [0-9]+;
FLOAT_VAL:    INT_VAL '.' INT_VAL;
STRING_VAL:   '"' ('\\' ["\\] | ~["\\\r\n])* '"' ;
// ---------------------- end types values ----------------------


// ---------------------- keywords ----------------------
DEF:          'def';
END:          'end';
RETURN:       'return';
IF:           'if';
ELSEIF:       'elseif';
ELSE:         'else';
DO:           'do';
LOOP:         'loop';
FOR:          'for';
IN:           'in';
BREAK:        'break';
NEXT:         'next';
// ---------------------- end keywords ----------------------


// ---------------------- assignments ----------------------
ASSIGN:       '=';
ADDASSIGN:    '+=';
DECASSIGN:    '-=';
MULTASSIGN:   '*=';
DIVASSIGN:    '/=';
MODASSIGN:    '%=';
// ---------------------- end assignments ----------------------


// ---------------------- logical operators ----------------------
AND:          '&&';
OR:           '||';
NOT:          '!';
APPEND:       '<<';
EQ:           '==' ;
NEQ:          '!=';
GTR:          '>';
GEQ:          '>=';
LES:          '<';
LEQ:          '<=';
// ---------------------- end logical operators ----------------------


// ---------------------- identifiers ----------------------
IDENTIFIER:   [a-zA-Z_][a-zA-Z0-9_]*;
PATTERNIND:   ('\n' | '\r')('\t|' | '    |'); // it's important that this line is above WS, to match  first
COMMENT:      '#' ~[\r\n]* -> skip;
MLCOMMENT:    '=begin' .*? '=end' -> skip;
WS:           [ \t\r\n] -> skip;
// ---------------------- end identifiers ----------------------

