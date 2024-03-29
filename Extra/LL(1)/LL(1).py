# Acknowledgement: https://www.geeksforgeeks.org/compiler-design-ll1-parser-in-python/
from Constants import *
from copy import deepcopy
from Samples.Sample_9 import *  # change this to import other sample files

def removeLeftRecursion(rulesDiction: dict):
    """
    for rule: A -> Aa | b
    result: A -> bA', A' -> aA' | ε
    """

    store = {}
    for lhs in rulesDiction:
        leftRecursion: list[list] = []
        withoutLeftRecursion: list[list] = []

        allrhs = rulesDiction[lhs]
        for subrhs in allrhs:
            if subrhs[0] == lhs:
                leftRecursion.append(subrhs[1:])
            else:
                withoutLeftRecursion.append(subrhs)

        if len(leftRecursion) != 0:
            lhs_ = make_prim(lhs)
            while (lhs_ in rulesDiction.keys()) or (lhs_ in store.keys()):
                lhs_ = make_prim(lhs_)

            for b in range(0, len(withoutLeftRecursion)):
                withoutLeftRecursion[b].append(lhs_)
            rulesDiction[lhs] = withoutLeftRecursion

            for a in range(0, len(leftRecursion)):
                leftRecursion[a].append(lhs_)
            leftRecursion.append([EPSILON])

            store[lhs_] = leftRecursion

    for left in store:
        rulesDiction[left] = store[left]

    return rulesDiction


def LeftFactoring(rulesDiction: dict):
    """
    for rule: A -> aDF | aCV | k
    result: A -> aA' | k, A' -> DF | CV
    """

    newDict: dict = {}

    for lhs in rulesDiction:
        allrhs = rulesDiction[lhs]

        temp: dict[str, list] = dict()
        for subrhs in allrhs:
            if subrhs[0] not in list(temp.keys()):
                temp[subrhs[0]] = [subrhs]
            else:
                temp[subrhs[0]].append(subrhs)

        new_rule = []
        tempo_dict = {}
        for term_key in temp:
            allStartingWithTermKey = temp[term_key]

            if len(allStartingWithTermKey) > 1:  # left factoring required
                lhs_ = make_prim(lhs)

                while (lhs_ in rulesDiction.keys()) or (lhs_ in tempo_dict.keys()):
                    lhs_ = make_prim(lhs_)

                new_rule.append([term_key, lhs_])
                ex_rules = []
                for g in temp[term_key]:
                    ex_rules.append(g[1:])
                tempo_dict[lhs_] = ex_rules

            else:
                new_rule.append(allStartingWithTermKey[0])

        newDict[lhs] = new_rule

        for key in tempo_dict:
            newDict[key] = tempo_dict[key]

    return newDict


def first(rule):
    """
    1) If x is terminal, then FIRST(x) = {x}
    2) If X → ε is production, then add ε to FIRST(X)
    3) If X is a non-terminal and X → PQR then FIRST(X) = FIRST(P)
       If FIRST(P) contains ε, then FIRST(X) = (FIRST(P) \ {ε}) U FIRST(QR)
    """

    global rules, nonterm_userdef, term_userdef, diction, firsts

    if len(rule) != 0 and (rule is not None):
        if rule[0] in term_userdef:
            return rule[0]
        elif rule[0] == EPSILON:
            return EPSILON

    if len(rule) != 0:
        if rule[0] in list(diction.keys()):

            fres = []
            rhs_rules = diction[rule[0]]

            for itr in rhs_rules:
                indivRes = first(itr)
                if type(indivRes) is list:
                    for i in indivRes:
                        fres.append(i)
                else:
                    fres.append(indivRes)

            if EPSILON not in fres:
                return fres
            else:
                newList = []
                fres.remove(EPSILON)
                if len(rule) > 1:
                    ansNew = first(rule[1:])
                    if ansNew != None:
                        if type(ansNew) is list:
                            newList = fres + ansNew
                        else:
                            newList = fres + [ansNew]
                    else:
                        newList = fres
                    return newList

                fres.append(EPSILON)
                return fres


def follow(nt):
    """
    1) For Start symbol, place $ in FOLLOW(S)
    2) If A → α B, then FOLLOW(B) = FOLLOW(A)
    3) If A → α B β, then
      If ε not in FIRST(β),
           FOLLOW(B) = FIRST(β)
      else do,
           FOLLOW(B) = (FIRST(β) \ {ε}) U FOLLOW(A)
    """

    global start_symbol, rules, nonterm_userdef, term_userdef, diction, firsts, follows

    solset = set()
    if nt == start_symbol:
        solset.add(STACK_END_INDICATOR)

    for curNT in diction:
        rhs = diction[curNT]

        for subrule in rhs:
            if nt in subrule:
                while nt in subrule:
                    index_nt = subrule.index(nt)
                    subrule = subrule[index_nt + 1 :]

                    if len(subrule) != 0:
                        res = first(subrule)

                        if EPSILON in res:
                            newList = []
                            res.remove(EPSILON)
                            ansNew = follow(curNT)
                            if ansNew != None:
                                if type(ansNew) is list:
                                    newList = res + ansNew
                                else:
                                    newList = res + [ansNew]
                            else:
                                newList = res
                            res = newList
                    else:
                        if nt != curNT:
                            res = follow(curNT)

                    if res is not None:
                        if type(res) is list:
                            for g in res:
                                solset.add(g)
                        else:
                            solset.add(res)

    return list(solset)


def computeAllFirsts():
    global rules, nonterm_userdef, term_userdef, diction, firsts
    for rule in rules:
        k = rule.split(TRANSITION)

        k[0] = k[0].strip()
        k[1] = k[1].strip()
        rhs = k[1]
        multirhs = rhs.split(DELIMITER)

        for i in range(len(multirhs)):
            multirhs[i] = multirhs[i].strip()
            multirhs[i] = multirhs[i].split()
        diction[k[0]] = multirhs

    print_rules("Rules before elimination of left recursion", RULES_FILE)

    diction = removeLeftRecursion(diction)
    print_rules("Rules after elimination of left recursion", LEFT_RECURSION_FILE)

    diction = LeftFactoring(diction)
    print_rules("Rules after left factoring", LEFT_FACTORING_FILE)

    for y in list(diction.keys()):
        t = set()
        for sub in diction.get(y):
            res = first(sub)
            if res != None:
                if type(res) is list:
                    for u in res:
                        t.add(u)
                else:
                    t.add(res)

        firsts[y] = t

    with open(FIRSTS_FILE, "w", encoding="utf-8") as f:
        f.write("Firsts:\n")
        key_list = list(firsts.keys())
        index = 0
        for gg in firsts:
            f.write(f"first({key_list[index]}) " f"=> {firsts.get(gg)}\n")
            index += 1


def print_rules(title: str, file: str):
    global diction

    with open(file, "w", encoding="utf-8") as f:
        f.write(f"\n{title}: \n")
        for y in diction:
            f.write(f"{y} {TRANSITION} {diction[y]}\n")


def computeAllFollows():
    global start_symbol, rules, nonterm_userdef, term_userdef, diction, firsts, follows
    for NT in diction:
        solset = set()
        sol = follow(NT)
        if sol is not None:
            for g in sol:
                solset.add(g)
        follows[NT] = solset

    with open(FOLLOWS_FILE, "w", encoding="utf-8") as f:
        f.write("Follows:\n")
        key_list = list(follows.keys())
        index = 0
        for gg in follows:
            f.write(f"follow({key_list[index]}) " f"=> {follows.get(gg)}\n")
            index += 1


def createParseTable():

    global diction, firsts, follows, term_userdef

    generate_first_follow()

    # create matrix of row(NT) x [col(T) + 1($)]
    ntlist = list(diction.keys())
    terminals = deepcopy(term_userdef)
    terminals.append(STACK_END_INDICATOR)

    mat = []
    for x in diction:
        row = []
        for y in terminals:
            row.append("")
        # of $ append one more col
        mat.append(row)

    grammar_is_LL = classify_grammar(ntlist, terminals, mat)

    generate_LA_table(ntlist, terminals, mat)

    return (mat, grammar_is_LL, terminals)


def add_line(line: str, module=31):
    modified_string = ""
    for i, char in enumerate(line):
        if (i + 1) % 31 == module // 2:
            modified_string += "|"
        else:
            modified_string += char
    return modified_string

def center_spaces(string: str, size=31):
    string = string.lstrip().rstrip()
    length = len(string)
    
    if length <= size:
        return string.center(size)
    return "too big!"
    
def generate_LA_table(ntlist, terminals, mat):
    with open(LA_TABLE_FILE, "w", encoding="utf-8") as f:
        f.write("Generated parsing table:\n\n")
        frmt = "{:>31}" * len(terminals)
        f.write(add_line(frmt.format(*terminals) + "\n"))
        f.write("\n")
        f.write("-" * (31 * len(terminals) + len(terminals) - 1) + "\n")
        f.write("\n")

        j = 0
        for y in mat:
            frmt1 = "{:>31}" * len(y)
            # for i in y:
            #     i = center_spaces(i)
            f.write(add_line(f"{ntlist[j]} {frmt1.format(*y):>31}" + "\n"))
            f.write("\n")
            f.write("-" * (31 * len(y) + len(y) - 1) + "\n")
            f.write("\n")
            j += 1


def generate_first_follow():

    global diction, firsts, follows, term_userdef

    with open(FIRST_FOLLOW_FILE, "w", encoding="utf-8") as f:
        f.write("Firsts and Follow Result table:\n")

        mx_len_first = 0
        mx_len_fol = 0
        for u in diction:
            k1 = len(str(firsts[u]))
            k2 = len(str(follows[u]))
            if k1 > mx_len_first:
                mx_len_first = k1
            if k2 > mx_len_fol:
                mx_len_fol = k2

        f.write(
            f"{{:<{10}}} "
            f"{{:<{mx_len_first + 5}}} "
            f"{{:<{mx_len_fol + 5}}}".format("Non-T", "FIRST", "FOLLOW") + "\n"
        )

        for u in diction:
            f.write(
                f"{{:<{10}}} "
                f"{{:<{mx_len_first + 5}}} "
                f"{{:<{mx_len_fol + 5}}}".format(u, str(firsts[u]), str(follows[u]))
                + "\n"
            )


def classify_grammar(ntlist, terminals, mat):
    grammar_is_LL = True

    for lhs in diction:
        rhs = diction[lhs]

        for y in rhs:
            res = first(y)
            if EPSILON in res:
                if type(res) == str:
                    firstFollow = []
                    fol_op = follows[lhs]
                    if fol_op is str:
                        firstFollow.append(fol_op)
                    else:
                        for u in fol_op:
                            firstFollow.append(u)
                    res = firstFollow
                else:
                    res.remove(EPSILON)
                    res = list(res) + list(follows[lhs])

            ttemp = []
            if type(res) is str:
                ttemp.append(res)
                res = deepcopy(ttemp)

            for c in res:
                xnt = ntlist.index(lhs)
                yt = terminals.index(c)

                if mat[xnt][yt] == "":
                    mat[xnt][yt] = mat[xnt][yt] + f"{lhs} {TRANSITION} {' '.join(y)}"
                else:
                    if f"{lhs} {TRANSITION} {y}" in mat[xnt][yt]:
                        continue
                    else:
                        grammar_is_LL = False
                        mat[xnt][yt] = (
                            mat[xnt][yt] + f",{lhs} {TRANSITION} {' '.join(y)}"
                        )

    return grammar_is_LL


def generate_stack_file(
    parsing_table, ll1, table_term_list, input_string, term_userdef, start_symbol
):
    stack = [start_symbol, STACK_END_INDICATOR]
    buffer = []

    input_string = input_string.split()
    input_string.reverse()
    buffer = [STACK_END_INDICATOR] + input_string
    matched = []

    with open(STACK_FILE, "w", encoding="utf-8") as f:

        f.write("{:>20} {:>20} {:>20} {:>20}".format("Matched", "Buffer", "Stack", "Action") + "\n")

        while True:
            if stack == [STACK_END_INDICATOR] and buffer == [STACK_END_INDICATOR]:
                f.write(
                    "{:>20} {:>20} {:>20} {:>20}".format(
                        " ".join(matched) ," ".join(buffer), " ".join(stack), "Valid"
                    )
                    + "\n"
                )
                return "Valid String!"

            elif stack[0] not in term_userdef:
                x = list(diction.keys()).index(stack[0])
                y = table_term_list.index(buffer[-1])

                if parsing_table[x][y] != "":
                    entry = parsing_table[x][y]
                    f.write(
                        "{:>20} {:>20} {:>20} {:>25}".format(
                            " ".join(matched),
                            " ".join(buffer),
                            " ".join(stack),
                            f"T[{stack[0]}][{buffer[-1]}] = {entry}",
                        )
                        + "\n"
                    )

                    lhs_rhs = entry.split(TRANSITION)
                    lhs_rhs[1] = lhs_rhs[1].replace(EPSILON, "").strip()
                    entryrhs = lhs_rhs[1].split()
                    stack = entryrhs + stack[1:]

                else:
                    return (
                        f"Invalid String! No rule at "
                        f"Table[{stack[0]}][{buffer[-1]}]."
                    )

            else:
                if stack[0] == buffer[-1]:  # stack top is Terminal
                    f.write(
                        "{:>20} {:>20} {:>20} {:>20}".format(
                            " ".join(matched), " ".join(buffer), " ".join(stack), f"Matched: {stack[0]}"
                        )
                        + "\n"
                    )

                    matched.append(stack[0])
                    buffer = buffer[:-1]
                    stack = stack[1:]
                else:
                    return "Invalid String! " "Unmatched terminal symbols"


def validateStringUsingStackBuffer(
    parsing_table, ll1, table_term_list, input_string, term_userdef, start_symbol
):

    print(f"Validate String => {input_string}")

    if not ll1:
        return f"Input String = " f'"{input_string}"\n' f"Grammar is not LL(1)"

    return generate_stack_file(
        parsing_table, ll1, table_term_list, input_string, term_userdef, start_symbol
    )


diction = {}
firsts = {}
follows = {}

computeAllFirsts()
start_symbol = list(diction.keys())[0]

computeAllFollows()

(parsing_table, result, tabTerm) = createParseTable()

if sample_input_string != None:
    validity = validateStringUsingStackBuffer(
        parsing_table, result, tabTerm, sample_input_string, term_userdef, start_symbol
    )
    print(validity)
else:
    print("No input String detected")