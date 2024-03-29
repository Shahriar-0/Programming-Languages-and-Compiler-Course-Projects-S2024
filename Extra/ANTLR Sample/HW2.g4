grammar HW2;

s
    : 'ab' EOF
    | a EOF
    | a b EOF
    ;

a
    : 'ab'  b
    ;

b
    : 'c'
    | 'cab'
    | 'c' s
    | 
    ;

