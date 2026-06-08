package com.project.www.service.impl;

import com.project.www.service.AuditLogService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service("coreAuditLogService")
@lombok.extern.slf4j.Slf4j
public class CoreAuditLogServiceImpl implements AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(CoreAuditLogServiceImpl.class);

    @Override
    public void log(String... args) {
        if (args != null && args.length > 0) {
            log.info("AUDIT LOG: {}", String.join(" | ", args));
        }
    }
}
