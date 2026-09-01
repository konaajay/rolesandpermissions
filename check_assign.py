import sys
import io

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

with open(r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java", "r", encoding="utf-8") as f:
    content = f.read()
    
start = content.find("public ProductAssignmentDto assignProduct")
print(content[start:start+1500])
