import re

file_path = r"C:\Users\ASUS\Downloads\ROLES AND PERMISSIONS\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www\vendor\service\impl\ReceivedProductServiceImpl.java"

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Remove ALL occurrences of @Transactional(readOnly = true)
content = re.sub(r'\s*@Transactional\(readOnly\s*=\s*true\)', '', content)

# Now add it back right before the relevant methods
content = re.sub(r'(@Override\s+public Page<ReceivedProductDto> getReceivedProducts)', r'@Transactional(readOnly = true)\n    \1', content)
content = re.sub(r'(@Override\s+public List<ReceivedProductDto> getReceivedProductsByRequirementId)', r'@Transactional(readOnly = true)\n    \1', content)
content = re.sub(r'(@Override\s+public Page<ProductAssignmentDto> getAssignmentsForProduct)', r'@Transactional(readOnly = true)\n    \1', content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Fixed Transactional cleanly")
