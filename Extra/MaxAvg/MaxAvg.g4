grammar MaxAvg;

@header {
    import java.util.ArrayList;
}

s returns [ArrayList<Double> maxList]:
    { $maxList = new ArrayList<Double>();}
    a1 = a {$maxList.add($a1.max);} '\n' s1 = s {$maxList.addAll($s1.maxList);} EOF
    {for (int i = 0; i < $maxList.size(); i++) System.out.println("max for day " + i + " is " + $maxList.get(i));}
    | {$maxList = new ArrayList<Double>();}; // empty string

a returns [double max]:
    {double max = 0;}
    b1 = b '*' a1 = a {$max = Math.max($b1.avg, $a1.max);}
    | {$max = 0;}; // empty string

b returns [double sum, int count, double avg]:
    {double sum = 0; int count = 0;}
    c1 = c ',' b1 = b {$sum = $c1.num + $b1.sum; $count = $c1.count + $b1.count; $avg = $sum / $count;}
    | {$avg = 0;}; // empty string

c returns [double num, int count]:
    n = FLOAT {$num = Double.parseDouble($n.text); $count = 1;}
    ;

FLOAT: ('-')? ('0'..'9')+ ('.' ('0'..'9')+)?;

WS: (' ' | '\t' | '\r')+ -> skip;
