from CFG import CFG
from Constants import *
from Rule import Rule

# Rules for the CFG

# Sample 1
# cfg = CFG(STARTING_SYMBOL, debug=False)
# cfg.add_rule(Rule("S", ["aSa", "aa"]))
# word = "aaaaaa"


# Sample 2
cfg = CFG("E", debug=False)
cfg.add_rule(Rule("E", ["Ta"]))
t_prim = map_char("T")
cfg.add_rule(Rule("T", ["b" + t_prim]))
cfg.add_rule(Rule(t_prim, ["(L)" + t_prim, "[E]" + t_prim, "a,L" + t_prim, "a" + t_prim, EPSILON]))
cfg.add_rule(Rule("L", ["E", "E,L", "b"]))
word = "b(ba)a"

if __name__ == "__main__":
    print("CFG Rules:")
    print(cfg)
    print()
    print(f"Is '{word}' in the language of the CFG? {cfg.recognize(word)}")
