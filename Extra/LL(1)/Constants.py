EPSILON = "ε"
TRANSITION = "→"
DELIMITER = " | "
PRIM = "'"
STACK_END_INDICATOR = "$"


def make_prim(non_terminal: str) -> str:
    """
    to make a new unique non-terminal
    """
    return non_terminal + PRIM