from collections import Counter
from itertools import combinations
import traceback
from graphviz import Digraph
from copy import deepcopy


from Constants import *
from Rule import Rule
from Node import Node


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
        self._graph = Digraph()

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

    def get_rules(self, left: str) -> list[Rule]:
        rules = []
        for rule in self._rules:
            if rule.left == left:
                for r in rule.right:
                    rules.append(r)
        return rules

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

    def __has_non_terminal(self, word: str) -> bool:
        return any(is_non_terminal(char) for char in word)

    def __match(self, word: str, created: str) -> tuple[int, Node]:

        if self._debug:
            print("*" * 50)
            print("Debugging CFG")
            print(f"Word: {word}")
            print(f"Created: {created}")
            traceback.print_stack() if self._stack_trace else None
            print("*" * 50)
            print()

        root = Node()
        count = 0
        while word and created:
            root_copy = deepcopy(root)
            if created[0] == word[0]:
                root_copy.add_child(Node(created[0]))
                word = word[1:]
                created = created[1:]
                count += 1

            elif is_non_terminal(created[0]):
                rules = self.get_rules(created[0])
                match = 0
                node : Node = None
                for rule in rules:
                    match, node = self.__match(word, rule)
                    if match:
                        break

                if not match:
                    return (0, Node())
                
                node.set_value(created[0])
                root_copy.add_child(deepcopy(node))
                word = word[match:]
                created = created[1:]
                count += match

            else:
                return (0, Node())

            root = deepcopy(root_copy)
            
        return (count, deepcopy(root)) if not created else (0, Node())

    def recognize(self, word: str) -> str:
        # self.remove_epsilon_rules()
        matched, root = self.__match(word, self._start_symbol)
        msg = (
            "True"
            if matched == len(word)
            else f"False, biggest match: {word[:matched]}"
        )
        root = root.get_children()[0]
        root.draw(self._graph, 1)
        self._graph.render("output", format="png", cleanup=True)
        return msg
