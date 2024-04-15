grammar FunctionCraft;


program
    : (funcDef | pattern)* main eof
    ;

main
    : DEF MAIN {System.out.println("MAIN");} LPAR RPAR funcBody END 
    ;

body
    : (statement)*
    ;

funcDef
    : DEF (name = IDENTIFIER) {System.out.println("FuncDec: " + $name.text);} funcArgs funcBody END 
    ;

funcArgs
    : LPAR ((((IDENTIFIER COMMA)* IDENTIFIER) (COMMA defaultArgs)?) | defaultArgs)? RPAR 
    ;
    // case1: a()
    // case2: a([])
    // case3: a(x1, x2, ...)
    // case4: a(x1, x2, ..., [])

defaultArgs
    : LBRACKET (IDENTIFIER ASSIGN expresion COMMA)* (IDENTIFIER ASSIGN expresion) RBRACKET
    ;

funcCallArgs
    : {System.out.println("FunctionCall");} LPAR ((expresion COMMA)* expresion)? RPAR
    ;


// left recursion removed:
// funcCall
//    : (IDENTIFIER | funcptr | listIndexing) funcCallArgs
//    | builtInFunc
//    ;

funcCall
//  (IDENTIFIER (LBRACKET expresion RBRACKET)* | method | lambdaFunc)
    : IDENTIFIER funcCallArgs funcCall_
    | builtInFunc funcCall_
    ;

funcCall_
    : funcCallArgs funcCall_
    | (LBRACKET expresion RBRACKET)+ funcCall_
    | epsilon
    ;

return
    : RETURN expresion? SEMICOLON {System.out.println("RETURN");}
    ;

// body! return for your own sake, the world is not functioning as you wish
funcBody
    : body return?
    ;

statement
    : assignment SEMICOLON
    | expresion SEMICOLON
    | if
    | loopDo
    | for
    ;

lambdaFunc
    : ARROW funcArgs LBRACE funcBody RBRACE {System.out.println("Structure: LAMBDA");}
    ;

method
    : METHOD LPAR COLON IDENTIFIER RPAR
    ;

funcptr
    : method
    | lambdaFunc
    | funcCall
    ;

list
    : (LBRACKET ((expresion COMMA)* expresion)? RBRACKET)
    ;

pattern
    : PATTERN (name = IDENTIFIER) funcArgs patternBody SEMICOLON {System.out.println("PatternDec: " + $name.text);}
    ;

patternBody
    : (PATTERNIND condition ASSIGN expresion)+
    ;

paternMatch
    : IDENTIFIER DOT MATCH funcCallArgs
    ;


condition
    : LPAR expresion RPAR
    ;

//expresion
//    : expresion APPEND logicalOr
//    | logicalOr
//    ;
expresion
    : logicalOr expresion_
    ;

expresion_
    : APPEND logicalOr expresion_
    | epsilon
    ;


value:
    funcCall
    | primitive
    | IDENTIFIER
    | par_exp
    ;

// priority: 1
par_exp
    : LPAR expresion RPAR
    ;

// priority: 2
listIndexing
    : (IDENTIFIER | funcCall) (LBRACKET expresion RBRACKET)+
    | value
    ;

// priority: 3
inPlaceAssignment
    : listIndexing (INC | DEC)
    | listIndexing
    ;

// priority: 4
not
    : (NOT | MINUS) inPlaceAssignment
    | inPlaceAssignment
    ;

// priority: 5
//multdiv
//    : multdiv (MULT | DIV) not
//    | not
//    ;
multdiv
    : not multdiv_
    ;
multdiv_
    : (MULT | DIV) not multdiv_
    | epsilon
    ;

// priority: 6
// addsub
//     : addsub (PLUS | MINUS) multdiv
//     | multdiv
//     ;
addsub
    : multdiv addsub_
    ;
addsub_
    : (PLUS | MINUS) multdiv addsub_
    | epsilon
    ;

// priority: 7
// compare
//     : compare (GTR | GEQ | LES | LEQ) addsub
//     | addsub
//     ;
compare
    : addsub compare_
    ;
compare_
    : (GTR | GEQ | LES | LEQ) addsub compare_
    | epsilon
    ;

// priority: 8
// eqcompare
//     : eqcompare (EQ | NEQ) compare
//     | compare
//     ;
eqcompare
    : compare eqcompare_
    ;
eqcompare_
    : (EQ | NEQ) compare eqcompare_
    | epsilon
    ;

// priority: 9
// logialAnd
//     : logialAnd AND eqcompare
//     | eqcompare
//     ;
logialAnd
    : eqcompare logialAnd_
    ;
logialAnd_
    : AND eqcompare logialAnd_
    | epsilon
    ;

// priority: 10
// logicalOr
//     : logicalOr OR logialAnd
//     | logialAnd
//     ;
logicalOr
    : logialAnd logicalOr_
    ;
logicalOr_
    : OR logialAnd logicalOr_
    | epsilon
    ;

// priority: 11
assignment
    : (name = IDENTIFIER) assigner expresion {System.out.println("Assignment: " + $name.text);}
    ;

// append

if
    : IF {System.out.println("Decision: IF");} condition body elif else END
    ;

elif
    : (ELSEIF {System.out.println("Decision: ELSEIF");} condition body)*
    ;

else
    :
    (ELSE {System.out.println("Decision: ELSE");} body)?
    ;

rangeGenerator
    : expresion RANGE expresion
    ;

loopCondition
    : (NEXT {System.out.println("Control: NEXT");} | BREAK {System.out.println("Control: NEXT");}) (IF condition)? SEMICOLON
    ;

loopBody
    : (statement | loopCondition)*
    ;

for
    : FOR {System.out.println("Loop: FOR");} IDENTIFIER IN LPAR rangeGenerator RPAR loopBody END
    ;

loopDo
    : LOOP {System.out.println("Loop: DO");} DO loopBody END
    ;

builtInFunc
    : {System.out.println("Built-In: PUTS");} puts
    | {System.out.println("Built-In: LEN");} len
    | {System.out.println("Built-In: CHOP");} chop
    | {System.out.println("Built-In: CHOMP");} chomp
    | {System.out.println("Built-In: PUSH");} push
    | {System.out.println("Built-In: MATCH");} paternMatch
    ;

puts
    : PUTS LPAR expresion RPAR
    ;

len
    : LEN LPAR expresion RPAR
    ;

chop
    : CHOP LPAR expresion RPAR
    ;

chomp
    : CHOMP LPAR expresion RPAR
    ;

push
    : PUSH LPAR expresion COMMA expresion RPAR
    ;

assigner
    : ASSIGN
    | ADDASSIGN
    | DECASSIGN
    | MULTASSIGN
    | DIVASSIGN
    | MODASSIGN
    ;

primitive
    : INT_VAL
    | FLOAT_VAL
    | STRING_VAL
    | TRUE
    | FALSE
    | list
    ;

epsilon
    :
    ;

eof
    : epsilon
    ;


////////////////////////////////////////////////////////////
// built-in functions

MAIN:         'main';
PATTERN:      'pattern';
PUTS:         'puts';
LEN:          'len';
PUSH:         'push';
CHOP:         'chop';
CHOMP:        'chomp';
MATCH:        'match';
METHOD:       'method';
////////////////////////////////////////////////////////////
// characters

PATTERNDELIM:   '|';
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
////////////////////////////////////////////////////////////
// types value

TRUE:         'true';
FALSE:        'false';
INT_VAL:      [0-9]+;
FLOAT_VAL:    INT_VAL '.' INT_VAL;
STRING_VAL:   '"' ('\\' ["\\] | ~["\\\r\n])* '"' ;
////////////////////////////////////////////////////////////
// statement keywords

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
////////////////////////////////////////////////////////////
// assignment

ASSIGN:       '=';
ADDASSIGN:    '+=';
DECASSIGN:    '-=';
MULTASSIGN:   '*=';
DIVASSIGN:    '/=';
MODASSIGN:    '%=';
////////////////////////////////////////////////////////////
// operations

AND:          '&&';
OR:           '||';
NOT:          '!';
APPEND:       '<<';
EQ:           'is' | '==' ; // TODO: delete is
NEQ:          'is not' | '!='; // TODO: delete is not
GTR:          '>';
GEQ:          '>=';
LES:          '<';
LEQ:          '<=';
////////////////////////////////////////////////////////////
// others

IDENTIFIER:   [a-zA-Z_][a-zA-Z0-9_]*;
PATTERNIND:   ('\n' | '\r')('\t|' | '    |'); // it's important that this line is above WS cause it's necessary to match it first
COMMENT:      '#' ~[\r\n]* -> skip;
MLCOMMENT:    '=begin' .*? '=end' -> skip;
WS:           [ \t\r\n] -> skip;
////////////////////////////////////////////////////////////
