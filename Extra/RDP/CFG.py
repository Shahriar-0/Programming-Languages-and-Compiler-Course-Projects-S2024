from collections import Counter
from itertools import combinations


from Constants import *
from Rule import Rule
import traceback


class CFG:
    def __init__(
        self,
        start_symbol: str,
        non_terminals: set[str] = set(),
        terminals: set[str] = set(),
        rules: list[Rule] = [],
        debug: bool = False,
        stack_trace: bool = False,
    ) -> None:
        self._start_symbol: str = start_symbol
        self._non_terminals: dict[str, int] = Counter(non_terminals)
        self._terminals: dict[str, int] = Counter(terminals)
        self._rules: list[Rule] = rules
        self._debug: bool = debug
        self._stack_trace: bool = stack_trace

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
                if (word == EPSILON or word.islower()) and not is_non_terminal(word):
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
        return any(
            rule.has_epsilon() and rule.left != self._start_symbol
            for rule in self._rules
        )

    def __change_start_symbol(self, new_start: str) -> None:
        self._start_symbol = new_start

    def remove_epsilon_rules(
        self,
    ) -> None:  # FIXME: This method is not working properly
        new_start = map_char(self._start_symbol)
        self.add_rule(Rule(new_start, [self._start_symbol]))
        self.__change_start_symbol(new_start)

        for rule in self._rules:
            if rule.has_epsilon():
                self.remove_rule(rule)
                for i in range(1, len(rule) + 1):
                    for comb in combinations(rule, i):
                        self.add_rule(Rule(rule.left, comb))

    def __first_non_terminal(self, word: str) -> int:
        for i, char in enumerate(word):
            if is_non_terminal(char):
                return i
        return None

    def __recognize(self, word: str, created: str) -> str:

        if self._debug:
            print("*" * 50)
            print("Debugging CFG")
            print(f"Word: {word}")
            print(f"Created: {created}")
            traceback.print_stack() if self._stack_trace else None
            print("*" * 50)
            print()

        if not created or word == created:
            return created
        
        if not word:
            return ""

        idx = self.__first_non_terminal(created)

        if idx is None:
            if len(word) > len(created):
                return created if word[:len(created)] == created else ""
            return ""
        
        if idx > len(word) or created[:idx] != word[:idx]:
            return ""
        
        for rule in self._rules:
            if rule.left == created[idx]:
                for r in rule.right:
                    """beware not to use next line
                    if self.__recognize(word, created[:idx] + r + created[idx + 1 :]):
                    the reason for that it is not equal to the RDP algorithm and recognizes 
                    some words that are not in the language of the CFG by using RDP"""
                    
                    remain_word = word[idx:]
                    remain_created = r + created[idx + 1:]
                    match = self.__recognize(remain_word, remain_created)
                    
                    if len(match) + len(created[idx + 1:]) > len(remain_word):
                        continue
                    
                    if match or (not match and remain_created == remain_word[:len(remain_created)]):
                        created = word[:idx] + match
                        return created
        return ""

    def recognize(self, word: str) -> str:
        # self.remove_epsilon_rules()
        matched = self.__recognize(word, self._start_symbol)
        msg = "True" if matched == word else f"False, biggest match: {matched}"
        return msg
