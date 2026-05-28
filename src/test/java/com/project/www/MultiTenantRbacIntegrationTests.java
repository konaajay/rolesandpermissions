package com.project.www;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.www.dto.*;
import com.project.www.entity.*;
import com.project.www.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MultiTenantRbacIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;


    @Autowired
    private com.project.www.config.DatabaseSeeder databaseSeeder;

    @Autowired
    private com.project.www.security.JwtService jwtService;

    static {
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/rbac_db?createDatabaseIfNotExist=true", "root", "root");
             java.sql.Statement stmt = conn.createStatement()) {
            try (java.sql.ResultSet rs = stmt.executeQuery("SHOW DATABASES")) {
                java.util.List<String> dbsToDrop = new java.util.ArrayList<>();
                while (rs.next()) {
                    String db = rs.getString(1);
                    if (db.startsWith("tenant_")) {
                        dbsToDrop.add(db);
                    }
                }
                for (String db : dbsToDrop) {
                    stmt.execute("DROP DATABASE IF EXISTS `" + db + "`");
                }
            }
        } catch (Exception e) {
            System.err.println("Static cleanup failed: " + e.getMessage());
        }
    }

    @MockitoBean
    private JavaMailSender javaMailSender;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() throws Exception {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
        tenantRepository.deleteAll();
        databaseSeeder.run();
    }

    @Test
    public void testCompleteRbacWorkflow() throws Exception {
        // 1. Register a new tenant "TenantA" and its initial admin user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setTenantName("TenantA");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("john.doe@tenanta.com");
        registerRequest.setPassword("securepassword");

        String registerResponseJson = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthResponse registerResponse = objectMapper.readValue(registerResponseJson, AuthResponse.class);
        assertNotNull(registerResponse.getToken());

        // Verify Tenant, Admin Role, and default Permissions were created in DB
        Tenant tenantA = tenantRepository.findByName("TenantA").orElse(null);
        assertNotNull(tenantA);
        assertTrue(tenantA.getActive());

        User adminUser = userRepository.findByEmailAndTenantId("john.doe@tenanta.com", tenantA.getId()).orElse(null);
        assertNotNull(adminUser);
        assertEquals("John", adminUser.getFirstName());
        assertEquals("SUPER_ADMIN", adminUser.getRole().getName());
        assertNotNull(adminUser.getEmployeeId());
        assertTrue(adminUser.getEmployeeId().endsWith("-001"));
        assertEquals("TEN", tenantA.getCode());

        // 2. Log in with the registered user
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setTenantId(tenantA.getId());
        loginRequest.setEmail("john.doe@tenanta.com");
        loginRequest.setPassword("securepassword");

        String loginResponseJson = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthResponse loginResponse = objectMapper.readValue(loginResponseJson, AuthResponse.class);
        assertNotNull(loginResponse.getToken());

        String jwtToken = "Bearer " + loginResponse.getToken();

        // 3. Create a new custom permission "REPORT_VIEW" for TenantA (requires PERMISSION_CREATE, which ADMIN has)
        CreatePermissionRequest permissionRequest = new CreatePermissionRequest();
        permissionRequest.setModule("REPORT");
        permissionRequest.setAction("VIEW");
        permissionRequest.setDescription("Allow viewing reports");

        mockMvc.perform(post("/permissions")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Permission Created Successfully")));

        Permission reportViewPerm = permissionRepository.findByPermissionKeyAndTenantId("REPORT_VIEW", tenantA.getId()).orElse(null);
        assertNotNull(reportViewPerm);

        // 4. Create a new custom role "MANAGER" with "REPORT_VIEW" permission for TenantA
        CreateRoleRequest roleRequest = new CreateRoleRequest();
        roleRequest.setName("MANAGER");
        roleRequest.setDescription("Manager Role");
        roleRequest.setPermissionIds(new HashSet<>(Collections.singletonList(reportViewPerm.getId())));

        mockMvc.perform(post("/roles")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Role Created Successfully")));

        Role managerRole = roleRepository.findByNameAndTenantId("MANAGER", tenantA.getId()).orElse(null);
        assertNotNull(managerRole);
        assertEquals(1, managerRole.getPermissions().size());

        // 5. Create a new User "Manager User" with "MANAGER" role (requires USER_CREATE, which ADMIN has)
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setFirstName("Jane");
        userRequest.setLastName("Smith");
        userRequest.setEmail("jane.smith@tenanta.com");
        userRequest.setPassword("managerpassword");
        userRequest.setRoleCode("MANAGER");

        mockMvc.perform(post("/users")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User Created Successfully")));

        User managerUser = userRepository.findByEmailAndTenantId("jane.smith@tenanta.com", tenantA.getId()).orElse(null);
        assertNotNull(managerUser);
        assertEquals("MANAGER", managerUser.getRole().getName());
        assertNotNull(managerUser.getEmployeeId());
        assertTrue(managerUser.getEmployeeId().endsWith("-002"));

        // 6. Test Tenant Isolation: attempt to get users of TenantA using a separate tenant request or wrong context
        // Ensure user list can be fetched with USER_VIEW authority
        mockMvc.perform(get("/users/tenant/" + tenantA.getId())
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", containsInAnyOrder("john.doe@tenanta.com", "jane.smith@tenanta.com")));

        // Attempting to query users with a mismatched tenant ID in path must fail
        mockMvc.perform(get("/users/tenant/999")
                        .header("Authorization", jwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Tenant mismatch")));
    }

    @Test
    public void testSuperAdminTenantCreation() throws Exception {
        // 1. Log in as seeded SUPER_ADMIN
        Tenant systemTenant = tenantRepository.findByName("System").orElse(null);
        assertNotNull(systemTenant);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setTenantId(systemTenant.getId());
        loginRequest.setEmail("superadmin@system.com");
        loginRequest.setPassword("superadmin");

        String loginResponseJson = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthResponse loginResponse = objectMapper.readValue(loginResponseJson, AuthResponse.class);
        String jwtToken = "Bearer " + loginResponse.getToken();

        // 2. Super admin creates a new Tenant dynamically via POST /tenants
        CreateTenantRequest createTenantRequest = new CreateTenantRequest();
        createTenantRequest.setTenantName("Acme Corp");
        createTenantRequest.setTenantCode("ACM");
        createTenantRequest.setAdminFirstName("Alice");
        createTenantRequest.setAdminLastName("Manager");
        createTenantRequest.setAdminEmail("alice@acme.com");
        createTenantRequest.setAdminPassword("acmepassword");

        String tenantResponseJson = mockMvc.perform(post("/tenants")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTenantRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TenantResponse tenantResponse = objectMapper.readValue(tenantResponseJson, TenantResponse.class);
        assertEquals("Acme Corp", tenantResponse.getName());
        assertEquals("ACM", tenantResponse.getCode());
        assertEquals("alice@acme.com", tenantResponse.getAdminEmail());

        // 3. Verify that default roles (ADMIN, EMPLOYEE) were created for the new tenant
        Tenant acmeTenant = tenantRepository.findById(tenantResponse.getId()).orElse(null);
        assertNotNull(acmeTenant);
        
        Role acmeAdminRole = roleRepository.findByNameAndTenantId("SUPER_ADMIN", acmeTenant.getId()).orElse(null);
        assertNotNull(acmeAdminRole);

        // 4. Verify that the admin user was created with a tenant-wise unique employee ID
        User acmeAdmin = userRepository.findByEmailAndTenantId("alice@acme.com", acmeTenant.getId()).orElse(null);
        assertNotNull(acmeAdmin);
        assertNotNull(acmeAdmin.getEmployeeId());
        assertTrue(acmeAdmin.getEmployeeId().startsWith("EMP-ACM-"));
        assertTrue(acmeAdmin.getEmployeeId().endsWith("-001"));
    }

    @Test
    public void testTenantAwareLoginWithHeader() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setTenantName("TenantHeaderTest");
        registerRequest.setFirstName("Bob");
        registerRequest.setLastName("Builder");
        registerRequest.setEmail("bob@header.com");
        registerRequest.setPassword("password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        Tenant tenant = tenantRepository.findByName("TenantHeaderTest").orElse(null);
        assertNotNull(tenant);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("bob@header.com");
        loginRequest.setPassword("password123");
        loginRequest.setTenantId(null);

        String loginResponseJson = mockMvc.perform(post("/auth/login")
                        .header("X-Tenant", tenant.getCode())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthResponse loginResponse = objectMapper.readValue(loginResponseJson, AuthResponse.class);
        assertNotNull(loginResponse.getToken());

        String extractedCode = jwtService.extractTenantCode(loginResponse.getToken());
        assertEquals(tenant.getCode(), extractedCode);
    }
}
