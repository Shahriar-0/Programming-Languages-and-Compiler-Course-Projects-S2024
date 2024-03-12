import random
from time import sleep

EPSILON = '_'


class Rule:
    def __init__(self, *args, probs = None) -> None:
        if(len(args) < 2):
            raise TypeError('a rule must include at least two params')
        if([type(arg).__name__ for arg in args] != ['str'] * len(args)):
            raise TypeError('a term must be only string')
        
        self.left = args[0]
        self.right = args[1:]
        self._len_drivs = len(self.right)

        if(probs != None and len(probs) != self._len_drivs):
            raise TypeError('invalid probs')
        elif(probs == None):
            probs = [1 / self._len_drivs] * self._len_drivs

        self.probs = probs

class CFG:
    def __init__(self, non_terminals: set, starting_symbol: str) -> None:
        if(len(starting_symbol) != 1):
            raise TypeError('invalid starting symbol')
        
        self._starting_symbol = starting_symbol
        self.non_terminals = non_terminals
        self.rules: dict[str, Rule]
        self.rules = {}

    def add_rule(self, rule: Rule) -> None:
        self.rules[rule.left] = rule

    def _evaluate(self) -> bool:
        n = 0
        for rule in self.rules.values():
            if(rule.left == self._starting_symbol):
                n += 1
        return (n == 1)
    
    def gen_random(self) -> str:
        if(not self._evaluate()):
            raise ValueError('bad strting sumbol')
        
        i = 0
        string = self._starting_symbol
        while(i < len(string)):
            if(string[i] in self.non_terminals):
                rule = self.rules[string[i]]
                string = string[: i] + random.choices(rule.right, k = 1, weights = rule.probs)[0] + string[i + 1 :]
                i = 0
            elif(string[i] == EPSILON):
                string = string[: i] + string[i + 1 :]
            else:
                i += 1
        return string


cfg = CFG({'S'}, 'S')
cfg.add_rule(Rule('S', '0S1', EPSILON, probs=[0.9, 0.1]))


for i in range(10):
    s = cfg.gen_random()
    print(s)
