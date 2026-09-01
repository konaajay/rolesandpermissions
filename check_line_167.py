with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java", "r", encoding="utf-8") as f:
    lines = f.readlines()

for i in range(165, 175):
    print(f"{i+1}: {lines[i].rstrip()}")
