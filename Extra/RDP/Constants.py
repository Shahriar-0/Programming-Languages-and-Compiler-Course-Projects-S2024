EPSILON = "ε"
TRANSITION = "→"
DELIMITER = " | "
STARTING_SYMBOL = "S"

original_non_terminals = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
terminals = "abcdefghijklmnopqrstuvwxyz[]{}()<>+-*/=,.;:!?@#$%^&|~1234567890"
new_non_terminal = "𝐴𝐵𝐶𝐷𝐸𝐹𝐺𝐻𝐼𝐽𝐾𝐿𝑀𝑁𝑂𝑃𝑄𝑅𝑆𝑇𝑈𝑉𝑊𝑋𝑌𝑍"

mapping = {
    original: new for original, new in zip(original_non_terminals, new_non_terminal)
}


def map_char(char: str) -> str:
    return mapping.get(char, char)


def is_non_terminal(char: str) -> bool:
    return char in new_non_terminal or char in original_non_terminals


def is_terminal(char: str) -> bool:
    return char in terminals
