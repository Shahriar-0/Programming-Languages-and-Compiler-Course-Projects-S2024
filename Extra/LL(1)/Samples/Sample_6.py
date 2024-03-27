# sample set 6
rules = [
    "E → T E'",
    "E' → + T E' | ε",
    "T → F T'",
    "T' → * F T' | ε",
    "F → ( E ) | id",
]
nonterm_userdef = ["E", "E'", "F", "T", "T'"]
term_userdef = ["id", "+", "*", "(", ")"]
sample_input_string = "id * * id"
# example string 1
# sample_input_string="( id * id )"
# example string 2
# sample_input_string="( id ) * id + id"
