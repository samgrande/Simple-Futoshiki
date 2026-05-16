import json

with open("app/src/main/res/raw/futo.json") as f:
    data = json.load(f)

# Find all color properties
colors = []
def find_colors(node):
    if isinstance(node, dict):
        if "c" in node and "k" in node["c"]:
            val = node["c"]["k"]
            if isinstance(val, list) and len(val) == 4 and isinstance(val[0], (int, float)):
                colors.append(val)
        for v in node.values():
            find_colors(v)
    elif isinstance(node, list):
        for item in node:
            find_colors(item)

find_colors(data)
print("Unique colors:", [list(x) for x in set(tuple(x) for x in colors)])
