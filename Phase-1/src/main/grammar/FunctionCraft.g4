grammar FunctionCraft;


program
    : (funcDef | pattern | comments)* main (comments)* eof
    ;

comments
    : (COMMENT | MLCOMMENT)+
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
    : LBRACKET (IDENTIFIER ASSIGN expression COMMA)* (IDENTIFIER ASSIGN expression) RBRACKET
    ;

pattern // FIXME: kinda sure that these don't have dafault arguments
    : PATTERN IDENTIFIER funcArgs patternBody SEMICOLON
    ;

patternBody
    : (PATTERNIND condition ASSIGN expression)+
    ;

main
    : DEF MAIN LPAR RPAR funcBody END
    ;

funcBody
    : body (RETURN (expression)? SEMICOLON)?
    ;

body
    : (statement)*
    ;

statement
    : assignment SEMICOLON // isn't this also an expression?
    | expression SEMICOLON
    | if        // draft: move these three to something called controlFlow dunno if it's a good idea
    | loopDo
    | for
    ;

assignment
    : (IDENTIFIER assigner expression)
    | (IDENTIFIER (INC | DEC))
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

expression
    : LPAR expression RPAR
    | expression numericOperator expression
    | expression booleanOperator expression
    | expression APPEND expression
    | funcCall
    | value
    | IDENTIFIER
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

funcCall
    : (IDENTIFIER | lambdaFunc | builtInFunc) funcCallArgs
    ;

funcCallArgs
    : LPAR ((expression COMMA)* expression)? RPAR
    ;

lambdaFunc
    : ARROW funcArgs LBRACE funcBody RBRACE
     // FIXME: kinda sure that these don't have dafault arguments
    ;

builtInFunc
    : PUTS
    | LEN
    | CHOP
    | CHOMP
    | PUSH
    | paternMatch
    ;

paternMatch
    : IDENTIFIER DOT MATCH
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

list
    : (LBRACKET ((expression COMMA)* expression)? RBRACKET)
    ;

funcptr
    : METHOD LPAR COLON IDENTIFIER RPAR
    | lambdaFunc
    ;

listIndexing
    : IDENTIFIER (LBRACKET expression RBRACKET)+
    ;

if
    : IF condition body (ELSEIF condition body)* (ELSE body)? END
    ;

condition
    : LPAR expression RPAR
    ;

loopDo
    : LOOP DO loopBody END
    ;

loopBody
    : (statement | loopCondition)*
    ;

loopCondition
    : (NEXT | BREAK) (IF condition)? SEMICOLON
    ;

for
    : FOR IDENTIFIER IN LPAR rangeGenerator RPAR loopBody END
    ;

rangeGenerator
    : expression RANGE expression
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
