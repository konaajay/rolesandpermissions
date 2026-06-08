import os

BASE_DIR = r"e:\ROLES AND PERMISSIONS\Project\src\main\java\com\project\www"
modules = ['vendor', 'user', 'tenant', 'accessmanagement', 'marketing']

for module in modules:
    module_entity_dir = os.path.join(BASE_DIR, module, 'entity')
    if not os.path.exists(module_entity_dir):
        continue
    for file in os.listdir(module_entity_dir):
        if file.endswith('.java'):
            duplicate_path = os.path.join(BASE_DIR, 'entity', file)
            if os.path.exists(duplicate_path):
                os.remove(duplicate_path)
                print(f"Removed duplicate entity: {duplicate_path}")

# Also do this for repository, service, dto, mapper, controller etc!
types = ['controller', 'service', 'repository', 'dto', 'mapper', 'exception']
for module in modules:
    for t in types:
        module_type_dir = os.path.join(BASE_DIR, module, t)
        if not os.path.exists(module_type_dir):
            continue
        for file in os.listdir(module_type_dir):
            if file.endswith('.java'):
                duplicate_path = os.path.join(BASE_DIR, t, file)
                if os.path.exists(duplicate_path):
                    os.remove(duplicate_path)
                    print(f"Removed duplicate {t}: {duplicate_path}")
        
        # also service/impl
        if t == 'service':
            impl_dir = os.path.join(module_type_dir, 'impl')
            if os.path.exists(impl_dir):
                for file in os.listdir(impl_dir):
                    if file.endswith('.java'):
                        duplicate_path = os.path.join(BASE_DIR, 'service', 'impl', file)
                        if os.path.exists(duplicate_path):
                            os.remove(duplicate_path)
                            print(f"Removed duplicate service impl: {duplicate_path}")
