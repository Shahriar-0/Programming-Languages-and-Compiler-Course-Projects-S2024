from Extra.RDP.Constants.Constants import *

class Rule:
    def __init__(self, left: str, rules: list[str]) -> None:
        self._left = left
        self._right = rules
        
    @property
    def left(self) -> str:
        return self._left
    
    @property
    def right(self) -> list[str]:
        return self._right

    def __getitem__(self, index: int) -> str:
        return self._right[index]

    def has_epsilon(self) -> bool:
        return EPSILON in self._right
    
    def get_rules_except_epsilon(self) -> list[str]:
        return [rule for rule in self._right if rule != EPSILON]
    
    def __len__(self) -> int:
        return len(self._right)
    
    def __str__(self) -> str:
        return f"{self._left} {TRANSITION} {DELIMITER.join(self._right)}"
    
    def __repr__(self) -> str:
        return self.__str__()
    
    def is_only_epsilon(self) -> bool:
        return len(self._right) == 1 and self._right[0] == EPSILON