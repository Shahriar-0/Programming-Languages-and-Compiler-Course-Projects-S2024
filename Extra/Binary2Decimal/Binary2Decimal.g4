grammar Binary2Decimal;

s returns [Double value]:
    { $value = 0; }
    v { $value = $v.value; }
    | v1 = v ',' v2 = v { $value = $v1.value + Math.pow(2, -1 * $v2.length) * $v2.value; }
    ;

v returns [Double value, int length]:
	'0' { $value = 0; $length = 1; }
	| '1' { $value = 1; $length = 1; }
	| '0' v0 = v { $value = $v0.value; $length = $v0.length + 1; }
	| '1' v1 = v { $value = $v1.value + Math.pow(2, $v1.length); $length = $v1.length + 1; }
	;
