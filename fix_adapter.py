import os

filepath = 'C:/Users/ASUS/Downloads/ROLES AND PERMISSIONS/ROLES AND PERMISSIONS/Project/src/main/java/com/project/www/integrations/event/SubscriptionPaymentIntegrationAdapter.java'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('tenantRepository.save(tenant);', '')

# We will save tenant inside updateTenantStatus method which uses DEFAULT context
method_to_add = '''
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public void updateTenantStatus(Tenant tenant) {
        String previousTenant = TenantContext.getCurrentTenantCode();
        try {
            TenantContext.clear();
            tenantRepository.save(tenant);
        } finally {
            if (previousTenant != null) {
                TenantContext.setCurrentTenantCode(previousTenant);
            }
        }
    }
'''

content = content.replace('log.info("Successfully updated subscription {} to {}", subscription.getId(), subscription.getStatus());', 
    'log.info("Successfully updated subscription {} to {}", subscription.getId(), subscription.getStatus());\n        \n        if ("SUCCESS".equalsIgnoreCase(status)) {\n            // Update the master DB tenant record separately\n            updateTenantStatus(tenant);\n        }')

content = content.replace('public void updateSubscriptionInsideTenantDB(String orderId, String status, Tenant tenant) {', method_to_add + '\n    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)\n    public void updateSubscriptionInsideTenantDB(String orderId, String status, Tenant tenant) {')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print('Fixed adapter tenant save logic')
