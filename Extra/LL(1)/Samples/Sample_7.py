# sample set 7 (left factoring & recursion present)
rules = [
    "S → A k O",
    "A → A d | a B | a C",
    "C → c",
    "B → b B C | r",
]

nonterm_userdef = ["A", "B", "C"]
term_userdef = ["k", "O", "d", "a", "c", "b", "r"]
sample_input_string = "a r k O"
