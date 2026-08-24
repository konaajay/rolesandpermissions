package com.project.www.integrations.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.project.www.integrations.repository",
        entityManagerFactoryRef = "integrationEntityManagerFactory",
        transactionManagerRef = "integrationTransactionManager"
)
public class IntegrationPersistenceConfig {

    @Bean(name = "integrationEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean integrationEntityManagerFactory(
            @Qualifier("masterDataSource") DataSource masterDataSource) {
            
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(masterDataSource);
        em.setPackagesToScan("com.project.www.integrations.entity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        em.setJpaPropertyMap(properties);
        em.setPersistenceUnitName("integration");

        return em;
    }

    @Bean(name = "integrationTransactionManager")
    public PlatformTransactionManager integrationTransactionManager(
            @Qualifier("integrationEntityManagerFactory") LocalContainerEntityManagerFactoryBean integrationEntityManagerFactory) {
        return new JpaTransactionManager(integrationEntityManagerFactory.getObject());
    }
}
