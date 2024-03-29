EPSILON = "ε"
TRANSITION = "→"
DELIMITER = " | "
PRIM = "'"
STACK_END_INDICATOR = "$"
OUTPUT_DIRECTORY = "./Output"
RULES_FILE = f"{OUTPUT_DIRECTORY}/rules.txt"
LEFT_RECURSION_FILE = f"{OUTPUT_DIRECTORY}/left_recursion.txt"
LEFT_FACTORING_FILE = f"{OUTPUT_DIRECTORY}/left_factoring.txt"
FIRSTS_FILE = f"{OUTPUT_DIRECTORY}/firsts.txt"
FOLLOWS_FILE = f"{OUTPUT_DIRECTORY}/follows.txt"
FIRST_FOLLOW_FILE = f"{OUTPUT_DIRECTORY}/firsts_follows.txt"
LA_TABLE_FILE = f"{OUTPUT_DIRECTORY}/table.txt"
STACK_FILE = f"{OUTPUT_DIRECTORY}/stack.txt"


def make_prim(non_terminal: str) -> str:
    """
    to make a new unique non-terminal
    """
    return non_terminal + PRIM
