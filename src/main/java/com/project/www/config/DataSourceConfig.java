package com.project.www.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;
import java.util.HashMap;
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

    @Bean(name = "masterDataSource")
    public DataSource masterDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(driverClassName);
        ds.setJdbcUrl(masterUrl);
        ds.setUsername(masterUsername);
        ds.setPassword(masterPassword);
        ds.setMaximumPoolSize(20);
        ds.setMinimumIdle(5);
        return ds;
    }

    @Bean
    @Primary
    @DependsOn("masterDataSource")
    public DynamicDataSourceManager dataSource(DataSource masterDataSource) {
        DynamicDataSourceManager dynamicDataSource = new DynamicDataSourceManager();
        Map<Object, Object> dataSources = new HashMap<>();
        
        dataSources.put("SYS", masterDataSource);
        dataSources.put("DEFAULT", masterDataSource);
        
        dynamicDataSource.setDefaultTargetDataSource(masterDataSource);
        dynamicDataSource.initDataSources(dataSources);

        return dynamicDataSource;
    }
}
