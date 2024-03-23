from collections import Counter
from itertools import combinations


from Constants import *
from Rule import Rule


class CFG:
    def __init__(
        self,
        start_symbol: str,
        non_terminals: set[str] = None,
        terminals: set[str] = None,
        rules: list[Rule] = None,
    ) -> None:
        self._start_symbol: str = start_symbol
        self._non_terminals: dict[str, int] = Counter(non_terminals)
        self._terminals: dict[str, int] = Counter(terminals)
        self._rules: list[Rule] = rules
    

    def __getitem__(self, index: int) -> Rule:
        return self._rules[index]

    def __len__(self) -> int:
        return len(self._rules)

    def get_non_terminals(self) -> list[str]:
        return self._non_terminals

    def get_terminals(self) -> list[str]:
        return self._terminals

    def get_start_symbol(self) -> str:
        return self._start_symbol

    def get_rules(self) -> list[Rule]:
        return self._rules

    def __str__(self) -> str:
        return "\n".join([str(rule) for rule in self._rules])

    def __repr__(self) -> str:
        return self.__str__()

    def __iter__(self):
        return iter(self._rules)

    def __contains__(self, item: Rule) -> bool:
        return item in self._rules

    def add_rule(self, rule: Rule) -> None:
        self._rules.append(rule)
        self._non_terminals[rule.left] = self._non_terminals.get(rule.left, 0) + 1
        for r in rule.right:
            for word in r:
                if (word == EPSILON or word.islower()) and not is_mapped(word):
                    self._terminals[word] = self._terminals.get(word, 0) + 1
            
    def remove_rule(self, rule: Rule) -> None:
        self._rules.remove(rule)
        self._non_terminals[rule.left] -= 1
        if self._non_terminals[rule.left] == 0:
            del self._non_terminals[rule.left]
        for r in rule.right:
            for word in r:
                if word == EPSILON or word.islower():
                    self._terminals[word] -= 1
                    if self._terminals[word] == 0:
                        del self._terminals[word]
                        
    def has_epsilon(self) -> bool:
        return any(rule.has_epsilon() and rule.left != self._start_symbol for rule in self._rules)

    def __change_start_symbol(self, new_start: str) -> None:
        self._start_symbol = new_start

    def remove_epsilon_rules(self) -> None: # FIXME: This method is not working properly
        new_start = map_char(self._start_symbol)
        self.add_rule(Rule(new_start, [self._start_symbol]))
        self.__change_start_symbol(new_start)
        
        for rule in self._rules:
            if rule.has_epsilon():
                self.remove_rule(rule)
                for i in range(1, len(rule) + 1):
                    for comb in combinations(rule, i):
                        self.add_rule(Rule(rule.left, comb))