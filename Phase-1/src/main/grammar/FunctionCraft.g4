grammar FunctionCraft;


program
    : (funcDef | pattern | comments)* main (comments)* eof
    ;

comments
    : (COMMENT | MLCOMMENT)+
    ;

main
    : DEF MAIN LPAR RPAR funcBody END
    ;

body
    : (statement)*
    ;

funcDef
    : DEF IDENTIFIER funcArgs funcBody END
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
    : LPAR ((expresion COMMA)* expresion)? RPAR
    ;

funcCall
    : (IDENTIFIER | lambdaFunc) funcCallArgs
    | builtInFunc
    ;

funcBody
    : body (RETURN (expresion)? SEMICOLON)?
    ;

statement
    : assignment SEMICOLON
    | expresion SEMICOLON
    | if
    | loopDo
    | for
    ;

lambdaFunc
    : ARROW funcArgs LBRACE funcBody RBRACE
     // FIXME: kinda sure that these don't have dafault arguments
    ;

funcptr
    : METHOD LPAR COLON IDENTIFIER RPAR
    | lambdaFunc
    ;

list
    : (LBRACKET ((expresion COMMA)* expresion)? RBRACKET)
    ;

pattern // FIXME: kinda sure that these don't have dafault arguments
    : PATTERN IDENTIFIER funcArgs patternBody SEMICOLON
    ;

patternBody
    : (PATTERNIND condition ASSIGN expresion)+
    ;

paternMatch
    : IDENTIFIER DOT MATCH funcCallArgs
    ;

assignment
    : (IDENTIFIER assigner expresion)
    | (IDENTIFIER (INC | DEC))
    ;

condition
    : LPAR expresion RPAR
    ;

expresion
    : LPAR expresion RPAR
    | expresion numericOperator expresion
    | expresion booleanOperator expresion
    | expresion APPEND expresion
    | funcCall
    | value
    | IDENTIFIER
    ;

if
    : IF condition body (ELSEIF condition body)* (ELSE body)? END
    ;

rangeGenerator
    : expresion RANGE expresion
    ;

loopCondition
    : (NEXT | BREAK) (IF condition)? SEMICOLON
    ;

loopBody
    : (statement | loopCondition)*
    ;

for
    : FOR IDENTIFIER IN LPAR rangeGenerator RPAR loopBody END
    ;

loopDo
    : LOOP DO loopBody END
    ;

builtInFunc
    : puts
    | len
    | chop
    | chomp
    | push
    | paternMatch
    ;

puts
    : PUTS LPAR expresion RPAR // I know you did this to only pass one parameter but i don't think that lexical should be responsible for this
                               // the better way i thinkg would be to use funcCallArgs, for this and other build-in functions
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

assigner // convert this to token?
        // assigner: [ADDASSIGN | DECASSIGN | MULTASSIGN | DIVASSIGN | MODASSIGN]
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
    | listIndexing
    ;

numericOperator
    : PLUS
    | MINUS
    | DIV
    | MULT
    | MOD
    ;

booleanOperator
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

eof
    : // epsilon
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
PATTERNIND:   '\t|' | '    |'; // it's important that this line is above WS cause it's necessary to match it first
COMMENT:      '#' ~[\r\n]* -> skip;
MLCOMMENT:    '=begin' .*? '=end' -> skip;
WS:           [ \t\r\n] -> skip;
////////////////////////////////////////////////////////////
