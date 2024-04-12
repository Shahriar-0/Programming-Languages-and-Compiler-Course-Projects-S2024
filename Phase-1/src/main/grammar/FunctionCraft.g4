grammar FunctionCraft;


program
    : (funcDef | pattern)* main
    ;

main
    : DEF MAIN LPAR RPAR funcBody END
    ;

funcDef
    : DEF IDENTIFIER funcArgs funcBody END
    ;

funcArgs
    : LPAR (((IDENTIFIER COMMA)* IDENTIFIER) (COMMA defaultArgs)? | defaultArgs)? RPAR
    ;
    // case1: a()
    // case2: a([])
    // case3: a(x1, x2, ...)
    // case4: a(x1, x2, ..., [])

defaultArgs
    : LBRACKET (IDENTIFIER ASSIGN expresion COMMA)* IDENTIFIER ASSIGN expresion RBRACKET
    ;

funcCallArgs
    : LPAR ((expresion COMMA)* expresion)? RPAR
    ;

funcCall
    : ((IDENTIFIER | lambdaFunc) funcCallArgs | builtInFunc)
    ;

funcBody
    : body (RETURN (expresion)? SEMICOLON)?
    ;

body
    : (statement)*
    ;

// cast:
// typeDef LPAR expresion RPAR;

append
    : expresion APPEND expresion
    ;

lambdaFunc
    : ARROW funcArgs LBRACE funcBody RBRACE
    ;


funcptr
    : METHOD LPAR COLON IDENTIFIER RPAR
    ;

list
    : (LBRACKET ((expresion COMMA)* expresion)? RBRACKET)
    ;

patternBody
    : (DELIMIETER condition ASSIGN expresion)+
    ;

pattern
    : PATTERN IDENTIFIER funcArgs patternBody SEMICOLON
    ;

paternMatch
    : IDENTIFIER DOT MATCH funcCallArgs
    ;

assignment
    : (IDENTIFIER assigner expresion | IDENTIFIER (INC | DEC)) SEMICOLON
    ;

statement
    : assignment
    | funcCall SEMICOLON
    | if
    | loopDo
    | for
    ;

condition
    : expresion
    ;

expresion
    : LPAR expresion RPAR
    | expresion numericOperation expresion
    | value
    | IDENTIFIER
    | funcCall
    | expresion boolOperation expresion
    | paternMatch
    | listIndexing
    | lambdaFunc
    ;

if
    : IF condition body (ELSEIF condition body)* (ELSE body)? END
    ;

for 
    : FOR IDENTIFIER IN LPAR expresion RANGE expresion RPAR loopBody END
    ;

loopCondition
    : (NEXT | BREAK) (IF condition)? SEMICOLON
    ;

loopBody
    : (statement | loopCondition)*
    ;

loopDo
    : LOOP DO loopBody END
    ;

builtInFunc
    : puts
    | len
    | chop
    | chomp
    | chomp
    | push
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

listIndexing
    : IDENTIFIER (LBRACKET expresion RBRACKET)+
    ;

value
    : INT_VAL 
    | FLOAT_VAL 
    | STRING_VAL 
    | TRUE 
    | FALSE 
    | list 
    | funcptr
    ;

numericOperation
    : PLUS 
    | MINUS 
    | DIV 
    | MULT 
    | MOD
    ;

boolOperation
    : AND
    | OR
    | NOT
    | EQ
    | NEQ
    | GTR
    | GEQ
    | LES
    | LEQ
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

DELIMIETER:   '|';
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
EQ:           'is' | '==' ;
NEQ:          'is not' | '!=';
GTR:          '>';
GEQ:          '>=';
LES:          '<';
LEQ:          '<=';
////////////////////////////////////////////////////////////
// others

IDENTIFIER:   [a-zA-Z_][a-zA-Z0-9_]*;
WS:           [ \t\r\n] -> skip;
COMMENT:      '#' ~[\r\n]* -> skip;
MLCOMMENT:    '=begin' .*? '=end' -> skip;
////////////////////////////////////////////////////////////
