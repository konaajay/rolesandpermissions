package com.project.www.service;

import com.project.www.enums.*;

import java.util.List;
import java.util.Optional;

import com.project.www.entity.CommissionRule;

public interface CommissionRuleService {
    List<CommissionRule> getAllRules();
    CommissionRule createRule(CommissionRule rule);
    Optional<CommissionRule> updateRule(Long id, CommissionRule ruleDetails);
    boolean deleteRule(Long id);
}
