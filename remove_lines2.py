with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java", "r", encoding="utf-8") as f:
    lines = f.readlines()

new_lines = []
for i in range(len(lines)):
    if i == 165 or i == 166 or i == 167: # Lines 166, 167, 168 (0-indexed 165, 166, 167)
        continue
    new_lines.append(lines[i])

with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java", "w", encoding="utf-8") as f:
    f.writelines(new_lines)
print("Removed lines 166-168")
