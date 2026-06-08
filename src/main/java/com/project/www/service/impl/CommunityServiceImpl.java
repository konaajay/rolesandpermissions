package com.project.www.service.impl;

import com.project.www.service.CommunityService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@lombok.extern.slf4j.Slf4j
public class CommunityServiceImpl implements CommunityService {

    private static final Logger log = LoggerFactory.getLogger(CommunityServiceImpl.class);

    @Override
    public void doSomething() {
        log.info("CommunityService: doSomething called");
    }

    @Override
    public void addLeadToCommunity(Long leadId, Long communityId, Long createdBy) {
        log.info("CommunityService: addLeadToCommunity called for leadId={}, communityId={}, createdBy={}", leadId, communityId, createdBy);
    }
}
