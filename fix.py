file_path = r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java"

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace double @Transactional
content = content.replace("@Transactional(readOnly = true)\n\n@Transactional(readOnly = true)", "@Transactional(readOnly = true)")
content = content.replace("@Transactional(readOnly = true)\r\n\r\n@Transactional(readOnly = true)", "@Transactional(readOnly = true)")
content = content.replace("@Transactional(readOnly = true)\n    \n    @Transactional(readOnly = true)", "@Transactional(readOnly = true)")
content = content.replace("@Transactional(readOnly = true)\r\n    \r\n    @Transactional(readOnly = true)", "@Transactional(readOnly = true)")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Fixed double Transactional")
