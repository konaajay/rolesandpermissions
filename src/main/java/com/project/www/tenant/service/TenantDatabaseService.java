package com.project.www.tenant.service;

import com.project.www.config.DynamicDataSourceManager;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
@RequiredArgsConstructor
public class TenantDatabaseService {

    private final DataSource masterDataSource;
    private final DynamicDataSourceManager dynamicDataSourceManager;

    @Value("${spring.datasource.url}")
    private String masterUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    public void provisionTenantDatabase(String tenantCode, String dbName) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(masterDataSource);

        // 1. Create the Database
        jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS " + dbName);

        // 2. Register the new data source into the dynamic routing manager
        HikariDataSource newDataSource = buildDataSource(dbName);
        dynamicDataSourceManager.addDataSource(tenantCode, newDataSource);

        // 3. Execute the schema script
        try {
            org.springframework.core.io.ClassPathResource schemaResource = new org.springframework.core.io.ClassPathResource("tenant_schema.sql");
            if (schemaResource.exists()) {
                org.springframework.jdbc.datasource.init.ResourceDatabasePopulator populator = new org.springframework.jdbc.datasource.init.ResourceDatabasePopulator(schemaResource);
                populator.execute(newDataSource);
                System.out.println("✅ Schema successfully generated for: " + dbName);
            } else {
                System.err.println("⚠️ Warning: tenant_schema.sql not found in resources. Database " + dbName + " is empty!");
            }
        } catch (Exception e) {
            System.err.println("❌ Error generating schema for " + dbName + ": " + e.getMessage());
        }
    }

    public void registerExistingTenantDatabase(String tenantCode, String dbName) {
        HikariDataSource dataSource = buildDataSource(dbName);
        dynamicDataSourceManager.addDataSource(tenantCode, dataSource);
        System.out.println("✅ Re-registered existing tenant data source for: " + tenantCode + " -> " + dbName);
    }

    /**
     * Shared factory for tenant HikariCP pools.
     * Keep pool sizes small — with many tenants, large pools exhaust MySQL max_connections.
     * Rule of thumb: maximumPoolSize = 2-3 per tenant for background/API workloads.
     */
    private HikariDataSource buildDataSource(String dbName) {
        String dbUrl = masterUrl.substring(0, masterUrl.lastIndexOf("/") + 1)
                + dbName + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(driverClassName);
        ds.setJdbcUrl(dbUrl);
        ds.setUsername(dbUsername);
        ds.setPassword(dbPassword);
        ds.setMaximumPoolSize(2);       // was 10 — too many connections with multiple tenants
        ds.setMinimumIdle(1);           // was 2
        ds.setConnectionTimeout(20000); // 20s timeout instead of default 30s
        ds.setIdleTimeout(300000);      // release idle connections after 5 min
        ds.setMaxLifetime(600000);      // recycle connections every 10 min
        ds.setPoolName("HikariPool-" + dbName);
        return ds;
    }
}

