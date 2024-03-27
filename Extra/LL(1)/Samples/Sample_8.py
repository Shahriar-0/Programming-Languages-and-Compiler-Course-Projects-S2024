# sample set 8 (Multiple char symbols T & NT)
rules = [
    "S → NP VP",
    "NP → P | PN | D N",
    "VP → V NP",
    "N → championship | ball | toss",
    "V → is | want | won | played",
    "P → me | I | you",
    "PN → India | Australia | Steve | John",
    "D → the | a | an",
]

nonterm_userdef = ["S", "NP", "VP", "N", "V", "P", "PN", "D"]
term_userdef = [
    "championship",
    "ball",
    "toss",
    "is",
    "want",
    "won",
    "played",
    "me",
    "I",
    "you",
    "India",
    "Australia",
    "Steve",
    "John",
    "the",
    "a",
    "an",
]
sample_input_string = "India won the championship"
