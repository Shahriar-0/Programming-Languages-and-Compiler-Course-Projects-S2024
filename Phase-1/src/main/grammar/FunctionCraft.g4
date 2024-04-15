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

lambdaFunc
    : ARROW funcArgs LBRACE funcBody RBRACE
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
