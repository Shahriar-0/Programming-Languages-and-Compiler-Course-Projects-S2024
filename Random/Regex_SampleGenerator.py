import re
import random

pattern = re.compile(r"([a-z]+_)*[a-z]+")

ans = ""
matches = []
not_matches = []

while len(matches) < 10 or len(not_matches) < 10:
    length = random.randint(10, 20)
    ans = "".join([random.choice("0123456789+*()abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-") for _ in range(length)])
    if re.fullmatch(pattern, ans):  # if re.match(pattern, ans):
        matches.append(ans)
    else:
        not_matches.append(ans)

print(f"----------matched:\n{matches[:10]}")
print(f"----------not matched:\n{not_matches[:10]}")