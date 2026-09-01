with open(r"C:\Users\ASUS\.gemini\antigravity\brain\69ff581a-7c0c-4a7f-81e6-528f7ec5224b\task.md", "r", encoding="utf-8") as f:
    content = f.read()

content = content.replace("`[ ]` 1", "`[x]` 1")
content = content.replace("`[ ]` 2", "`[x]` 2")
content = content.replace("`[ ]` 3", "`[x]` 3")
content = content.replace("`[ ]` 4", "`[x]` 4")
content = content.replace("`[ ]` 5", "`[x]` 5")
content = content.replace("`[ ]` 6", "`[/]` 6")

with open(r"C:\Users\ASUS\.gemini\antigravity\brain\69ff581a-7c0c-4a7f-81e6-528f7ec5224b\task.md", "w", encoding="utf-8") as f:
    f.write(content)
print("Updated task.md")
