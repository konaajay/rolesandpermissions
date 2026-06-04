package com.project.www.service.impl;

import com.project.www.enums.*;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.project.www.entity.CommissionRule;
import com.project.www.repository.CommissionRuleRepository;
import com.project.www.service.CommissionRuleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommissionRuleServiceImpl implements CommissionRuleService {

    private final CommissionRuleRepository repository;

    @Override
    public List<CommissionRule> getAllRules() {
        return repository.findAll();
    }

    @Override
    public CommissionRule createRule(CommissionRule rule) {
        return repository.save(rule);
    }

    @Override
    public Optional<CommissionRule> updateRule(Long id, CommissionRule ruleDetails) {

        CommissionRule rule = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found: " + id));

        rule.setCourseId(ruleDetails.getCourseId());
        rule.setAffiliatePercent(ruleDetails.getAffiliatePercent());
        rule.setStudentDiscountPercent(ruleDetails.getStudentDiscountPercent());
        rule.setBonus(ruleDetails.isBonus());
        rule.setActive(ruleDetails.isActive());

        return Optional.of(repository.save(rule));
    }

    @Override
    public boolean deleteRule(Long id) {

        CommissionRule rule = repository.findById(id)
                .orElse(null);
        
        if (rule != null) {
            repository.delete(rule);
            return true;
        }
        return false;
    }
}
