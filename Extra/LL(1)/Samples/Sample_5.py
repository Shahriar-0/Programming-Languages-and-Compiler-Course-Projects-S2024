# sample set 5 (With left recursion)
rules = [
    "A → B C c | g D B",
    "B → b C D E | ε",
    "C → D a B | c a",
    "D → ε | d D",
    "E → E a f | c",
]
nonterm_userdef = ["A", "B", "C", "D", "E"]
term_userdef = ["a", "b", "c", "d", "f", "g"]
sample_input_string = "b a c a c"
