with open(r"C:\Users\ASUS\.gemini\antigravity\brain\69ff581a-7c0c-4a7f-81e6-528f7ec5224b\task.md", "r", encoding="utf-8") as f:
    content = f.read()

content = content.replace("`[ ]` 8", "`[x]` 8")

with open(r"C:\Users\ASUS\.gemini\antigravity\brain\69ff581a-7c0c-4a7f-81e6-528f7ec5224b\task.md", "w", encoding="utf-8") as f:
    f.write(content)
