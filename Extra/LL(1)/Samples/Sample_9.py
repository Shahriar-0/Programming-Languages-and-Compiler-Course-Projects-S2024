# sample set 9 (Without left recursion)
rules = [
    "S → X a Y | Y b X",
    "X → c X X' | ε",
    "X' → d X' c | a",
    "Y → ε | e Y b | f Y'",
    "Y' → e Y' | ε",
]
nonterm_userdef = ["S", "X", "X'", "Y", "Y'"]
term_userdef = ["a", "b", "c", "d", "e", "f"]
sample_input_string = "c c c d a c d a c a a e f e b"
