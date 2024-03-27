# sample set 1 (Result: Not LL(1))
rules = ["A → S B | B", "S → a | B c | ε", "B → b | d"]
nonterm_userdef = ["A", "S", "B"]
term_userdef = ["a", "c", "b", "d"]
sample_input_string = "b c b"
