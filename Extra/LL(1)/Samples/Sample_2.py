# sample set 2 (Result: LL(1))
rules = ["S → A | B C", "A → a | b", "B → p | ε", "C → c"]
nonterm_userdef = ["A", "S", "B", "C"]
term_userdef = ["a", "c", "b", "p"]
sample_input_string = "p c"
