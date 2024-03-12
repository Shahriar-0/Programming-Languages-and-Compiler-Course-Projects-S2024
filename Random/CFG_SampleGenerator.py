import random

EPSILON = "_"

class Rule:
    def __init__(self, left, *rules, probs=None, rand=True) -> None:
        if left is None or type(left).__name__ != "str":
            raise TypeError("invalid left")
        if len(rules) == 0:
            raise TypeError("invalid rules")
        if [type(arg).__name__ for arg in rules] != ["str"] * len(rules):
            raise TypeError("a term must be only string")

        self._left = left
        self._right = rules
        self._len_drivs = len(self._right)
        self._rand = rand

        if probs != None and len(probs) != self._len_drivs:
            raise TypeError("invalid probs")
        elif probs == None:
            probs = (
                [random.random() + 1 for _ in range(self._len_drivs)]
                if rand
                else [1 / self._len_drivs] * self._len_drivs
            )
            probs = [p / sum(probs) for p in probs]

        self._probs = probs

    @property
    def left(self):
        return self._left
    
    def rerandomize(self):
        if self._rand:
            self._probs = (
                [random.random() + 1 for _ in range(self._len_drivs)]
            )
            self._probs = [p / sum(self._probs) for p in self._probs]
        else:
            pass


class CFG:
    def __init__(self, starting_symbol: str) -> None:
        if len(starting_symbol) != 1:
            raise TypeError("invalid starting symbol")

        self._starting_symbol = starting_symbol
        self._non_terminals = set()
        self._rules: dict[str, Rule] = {}
        self._stagnant_threshold = 50

    def add_rule(self, rule: Rule) -> None:
        self._rules[rule._left] = rule
        self._non_terminals.update(rule.left)

    def _is_over(self, string: str) -> bool:
        return all([c not in self._non_terminals for c in string]) and all(
            [c != EPSILON for c in string]
        )
        
    def _rerandomize(self):
        for rule in self._rules.values():
            rule.rerandomize()

    def gen_random(self) -> str:
        self._rerandomize()
        output = self._starting_symbol
        stagnant_count = 0
        while not self._is_over(output):
            if stagnant_count > self._stagnant_threshold:
                self._rerandomize()
            for i in range(len(output)):
                if output[i] in self._non_terminals:
                    rule = self._rules[output[i]]
                    output = (
                        output[:i]
                        + random.choices(rule._right, k=1, weights=rule._probs)[0]
                        + output[i + 1 :]
                    )
                    break
                elif output[i] == EPSILON:
                    output = output[:i] + output[i + 1 :]
                    break
            stagnant_count += 1
        return output


cfg = CFG(starting_symbol="S")

# Sample 1
# cfg.add_rule(Rule("S", "0S1", "1S0", EPSILON))

# Sample 2
cfg.add_rule(Rule("S", "[K]"))
cfg.add_rule(Rule("K", "L", EPSILON))
cfg.add_rule(Rule("L", "L,L", "[K]", "X"))
cfg.add_rule(Rule("X", "X,X", "T"))
cfg.add_rule(Rule("T", "0", "1"))


s = set()
while len(s) < 10:
    s.add(cfg.gen_random())

for i in s:
    print(f'"{i}"')
