grammar Encryption;

@header {
    import main.java.Encryption.*;
}

s returns [String value]
    : a1 = a[true] { $value = $a1.value; }
    ;

a [boolean shouldConvert] returns [String value, String Converted]
    : l1 = l[$shouldConvert] { $Converted = $l1.Converted; boolean newShouldConvert = $l1.isNotVowel;} 
    a1 = a[newShouldConvert] { $value = $l1.value + $a1.value; }
    | l1 = l[$shouldConvert] { $value = $l1.value; $Converted = $l1.Converted; $value = $Converted;}
    ;

l [boolean shouldConvert] returns [String value, String Converted, boolean isNotVowel]:
    c1 = LETTER { $value = Encryption.encrypt($c1.text, shouldConvert); $Converted = $value; $isNotVowel = Encryption.isNotVowel($Converted);}
    ;


LETTER: [a-zA-Z];