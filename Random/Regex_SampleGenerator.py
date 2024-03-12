import re
import random

pattern = re.compile(r"(\d+)|(\+)|(\*)|(\()|(\))")

ans = ""
matches = []

while len(matches) < 10:
    length = random.randint(1, 40)
    ans = "".join([random.choice("0123456789+*()abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ") for _ in range(length)])
    if re.match(pattern, ans):
        matches.append(ans)

print(matches)