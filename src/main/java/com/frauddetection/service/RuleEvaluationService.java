package com.frauddetection.service;

import com.frauddetection.model.FraudRule;
import com.frauddetection.model.Transaction;
import com.frauddetection.repository.FraudRuleRepository;
import com.frauddetection.rules.RuleEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RuleEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(RuleEvaluationService.class);

    private final FraudRuleRepository fraudRuleRepository;
    private final List<RuleEngine> ruleEngines;

    public RuleEvaluationService(FraudRuleRepository fraudRuleRepository, List<RuleEngine> ruleEngines) {
        this.fraudRuleRepository = fraudRuleRepository;
        this.ruleEngines = ruleEngines;
    }

    public RuleEvaluationResult evaluate(Transaction transaction) {
        List<FraudRule> activeRules = fraudRuleRepository.findByEnabledTrue();
        Map<FraudRule.RuleType, RuleEngine> engineMap = ruleEngines.stream()
                .collect(Collectors.toMap(RuleEngine::getSupportedType, Function.identity()));

        List<String> triggeredRules = new ArrayList<>();

        for (FraudRule rule : activeRules) {
            RuleEngine engine = engineMap.get(rule.getRuleType());
            if (engine == null) {
                log.warn("No engine found for rule type: {}", rule.getRuleType());
                continue;
            }
            try {
                if (engine.evaluate(transaction, rule)) {
                    triggeredRules.add(rule.getName());
                }
            } catch (Exception e) {
                log.error("Error evaluating rule '{}': {}", rule.getName(), e.getMessage());
            }
        }

        return new RuleEvaluationResult(triggeredRules, !triggeredRules.isEmpty());
    }

    public static class RuleEvaluationResult {
        private final List<String> triggeredRules;
        private final boolean triggered;

        public RuleEvaluationResult(List<String> triggeredRules, boolean triggered) {
            this.triggeredRules = triggeredRules;
            this.triggered = triggered;
        }

        public List<String> getTriggeredRules() { return triggeredRules; }
        public boolean isTriggered() { return triggered; }
    }
}
