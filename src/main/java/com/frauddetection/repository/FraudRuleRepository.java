package com.frauddetection.repository;

import com.frauddetection.model.FraudRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudRuleRepository extends JpaRepository<FraudRule, Long> {

    List<FraudRule> findByEnabledTrue();

    List<FraudRule> findByRuleType(FraudRule.RuleType ruleType);

    boolean existsByName(String name);
}
