# sample set 3 (Result: LL(1))
rules = ["S → A B | C", "A → a | b | ε", "B → p | ε", "C → c"]
nonterm_userdef = ["A", "S", "B", "C"]
term_userdef = ["a", "c", "b", "p"]
sample_input_string = "a c b"
