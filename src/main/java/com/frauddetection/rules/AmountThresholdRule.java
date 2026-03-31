package com.frauddetection.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frauddetection.model.FraudRule;
import com.frauddetection.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AmountThresholdRule implements RuleEngine {

    private static final Logger log = LoggerFactory.getLogger(AmountThresholdRule.class);
    private final ObjectMapper objectMapper;

    public AmountThresholdRule(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean evaluate(Transaction transaction, FraudRule rule) {
        try {
            JsonNode params = objectMapper.readTree(rule.getParamJson());
            double maxAmount = params.has("maxAmount") ? params.get("maxAmount").asDouble(5000.0) : 5000.0;
            boolean triggered = transaction.getAmount().compareTo(BigDecimal.valueOf(maxAmount)) > 0;
            if (triggered) {
                log.info("AmountThresholdRule triggered: txId={}, amount={}, threshold={}",
                        transaction.getId(), transaction.getAmount(), maxAmount);
            }
            return triggered;
        } catch (Exception e) {
            log.warn("AmountThresholdRule parse error for rule {}: {}", rule.getId(), e.getMessage());
            return transaction.getAmount().compareTo(BigDecimal.valueOf(5000.0)) > 0;
        }
    }

    @Override
    public FraudRule.RuleType getSupportedType() {
        return FraudRule.RuleType.AMOUNT_THRESHOLD;
    }
}
