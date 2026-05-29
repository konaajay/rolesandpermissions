package com.project.www.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String masterUrl;

    @Value("${spring.datasource.username}")
    private String masterUsername;

    @Value("${spring.datasource.password}")
    private String masterPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Bean
    public DataSource masterDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(driverClassName);
        ds.setJdbcUrl(masterUrl);
        ds.setUsername(masterUsername);
        ds.setPassword(masterPassword);
        ds.setMaximumPoolSize(5);
        ds.setMinimumIdle(1);
        return ds;
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSource masterDataSource) {
        TenantRoutingDataSource routingDataSource = new TenantRoutingDataSource();
        Map<Object, Object> targetDataSources = new java.util.concurrent.ConcurrentHashMap<>();

        // Register master database
        targetDataSources.put("master", masterDataSource);

        // Load and register existing active tenants
        try (Connection connection = masterDataSource.getConnection();
                Statement statement = connection.createStatement()) {

            try (ResultSet rs = statement.executeQuery("SELECT code, db_name, db_user, db_password FROM tenants WHERE active = 1")) {
                while (rs.next()) {
                    String code = rs.getString("code");
                    String dbName = rs.getString("db_name");
                    String dbUser = rs.getString("db_user");
                    String dbPassword = rs.getString("db_password");

                    if (dbName != null && !dbName.trim().isEmpty()) {
                        DataSource tenantDs = createTenantDataSource(dbName, dbUser, dbPassword);
                        targetDataSources.put(code, tenantDs);
                    }
                }
            } catch (Exception e) {
                // Table might not exist yet on very first run, ignore.
                System.out.println("No existing tenants loaded on startup. 'tenants' table might not exist yet.");
            }
        } catch (Exception e) {
            System.err.println("Could not load tenants on startup: " + e.getMessage());
        }

        routingDataSource.setDefaultTargetDataSource(masterDataSource);
        routingDataSource.setTargetDataSourcesMap(targetDataSources);
        routingDataSource.afterPropertiesSet();

        return routingDataSource;
    }

    public DataSource createTenantDataSource(String dbName, String dbUser, String dbPassword) {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(driverClassName);

        String username = (dbUser != null && !dbUser.trim().isEmpty()) ? dbUser : masterUsername;
        String password = (dbPassword != null && !dbPassword.trim().isEmpty()) ? dbPassword : masterPassword;

        String tenantUrl = masterUrl;
        if (masterUrl.contains("jdbc:mysql://")) {
            int schemeEnd = masterUrl.indexOf("jdbc:mysql://") + "jdbc:mysql://".length();
            int pathStart = masterUrl.indexOf("/", schemeEnd);
            int queryStart = masterUrl.indexOf("?", pathStart);

            String hostAndPort = (pathStart != -1) ? masterUrl.substring(schemeEnd, pathStart)
                    : masterUrl.substring(schemeEnd);
            String queryParams = (queryStart != -1) ? masterUrl.substring(queryStart) : "";
            
            if (queryParams.contains("createDatabaseIfNotExist=true")) {
                queryParams = queryParams.replace("createDatabaseIfNotExist=true", "");
                queryParams = queryParams.replace("?&", "?").replace("&&", "&");
                if (queryParams.endsWith("?") || queryParams.endsWith("&")) {
                    queryParams = queryParams.substring(0, queryParams.length() - 1);
                }
            }

            tenantUrl = "jdbc:mysql://" + hostAndPort + "/" + dbName + queryParams;
        }

        ds.setJdbcUrl(tenantUrl);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setMaximumPoolSize(2);
        ds.setMinimumIdle(1);
        ds.setIdleTimeout(10000);
        ds.setMaxLifetime(30000);
        return ds;
    }
}
