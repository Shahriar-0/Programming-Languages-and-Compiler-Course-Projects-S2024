# sample set 4 (Result: Not LL(1))
rules = ["S → A B C | C", "A → a | b B | ε", "B → p | ε", "C → c"]
nonterm_userdef = ["A", "S", "B", "C"]
term_userdef = ["a", "c", "b", "p"]
sample_input_string = "b p p c"
