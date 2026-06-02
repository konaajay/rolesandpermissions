package com.project.www.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicDataSourceManager extends AbstractRoutingDataSource {

    private final Map<Object, Object> dataSources = new ConcurrentHashMap<>();

    @Override
    protected Object determineCurrentLookupKey() {
        return com.project.www.util.TenantContext.getCurrentTenantCode(); // e.g. "SYS", "HRM"
    }

    public void addDataSource(String tenantCode, DataSource dataSource) {
        dataSources.put(tenantCode, dataSource);
        this.setTargetDataSources(dataSources);
        this.afterPropertiesSet(); // Re-initialize the routing data source
    }

    public void initDataSources(Map<Object, Object> initialDataSources) {
        dataSources.putAll(initialDataSources);
        this.setTargetDataSources(dataSources);
        this.afterPropertiesSet();
    }
}
