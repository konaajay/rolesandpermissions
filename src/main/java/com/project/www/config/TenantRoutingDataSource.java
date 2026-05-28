package com.project.www.config;

import com.project.www.util.TenantContext;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;

public class TenantRoutingDataSource extends AbstractRoutingDataSource implements org.springframework.beans.factory.DisposableBean {

    private final Map<Object, Object> targetDataSourcesMap = new ConcurrentHashMap<>();

    public void setTargetDataSourcesMap(Map<Object, Object> targetDataSources) {
        this.targetDataSourcesMap.clear();
        this.targetDataSourcesMap.putAll(targetDataSources);
        super.setTargetDataSources(this.targetDataSourcesMap);
    }

    public synchronized void addDataSource(String tenantCode, DataSource dataSource) {
        if (!this.targetDataSourcesMap.containsKey(tenantCode)) {
            this.targetDataSourcesMap.put(tenantCode, dataSource);
            super.setTargetDataSources(this.targetDataSourcesMap);
            super.afterPropertiesSet();
        }
    }

    public boolean containsDataSource(String tenantCode) {
        return this.targetDataSourcesMap.containsKey(tenantCode);
    }

    public DataSource getDataSource(String tenantCode) {
        return (DataSource) this.targetDataSourcesMap.get(tenantCode);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String tenantCode = TenantContext.getCurrentTenantCode();
        return (tenantCode != null) ? tenantCode : "master";
    }

    @Override
    public void destroy() throws Exception {
        for (Object ds : this.targetDataSourcesMap.values()) {
            if (ds instanceof com.zaxxer.hikari.HikariDataSource) {
                try {
                    ((com.zaxxer.hikari.HikariDataSource) ds).close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        this.targetDataSourcesMap.clear();
    }
}
