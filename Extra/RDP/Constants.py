EPSILON = "ε"
TRANSITION = "→"
DELIMITER = " | "

original_chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
new_chars = "𝐴𝐵𝐶𝐷𝐸𝐹𝐺𝐻𝐼𝐽𝐾𝐿𝑀𝑁𝑂𝑃𝑄𝑅𝑆𝑇𝑈𝑉𝑊𝑋𝑌𝑍"

mapping = {original: new for original, new in zip(original_chars, new_chars)}


def map_char(char: str) -> str:
    return mapping.get(char, char)

def is_mapped(char: str) -> bool:
    return char in new_chars
