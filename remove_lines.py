with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java", "r", encoding="utf-8") as f:
    lines = f.readlines()

new_lines = []
for i in range(len(lines)):
    if i == 315 or i == 316:
        continue # skip 316 and 317 (0-indexed 315, 316)
    new_lines.append(lines[i])

with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java", "w", encoding="utf-8") as f:
    f.writelines(new_lines)
print("Removed lines 316 and 317")
