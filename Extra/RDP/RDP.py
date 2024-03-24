from CFG import CFG
from Constants import *
from Rule import Rule

# Rules for the CFG
# cfg = CFG(STARTING_SYMBOL)
cfg = CFG(STARTING_SYMBOL, debug=True)

cfg.add_rule(Rule("S", ["aSa", "aa"]))

word = "aaaaaa"

if __name__ == "__main__":
    print("CFG Rules:")
    print(cfg)
    print()
    print(f"Is '{word}' in the language of the CFG? {cfg.recognize(word)}")
