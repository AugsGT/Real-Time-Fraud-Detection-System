package com.frauddetection.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frauddetection.model.FraudRule;
import com.frauddetection.model.Transaction;
import com.frauddetection.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class FrequencyRule implements RuleEngine {

    private static final Logger log = LoggerFactory.getLogger(FrequencyRule.class);
    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;

    public FrequencyRule(TransactionRepository transactionRepository, ObjectMapper objectMapper) {
        this.transactionRepository = transactionRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean evaluate(Transaction transaction, FraudRule rule) {
        try {
            JsonNode params = objectMapper.readTree(rule.getParamJson());
            int maxCount = params.has("maxCount") ? params.get("maxCount").asInt(10) : 10;
            int windowMinutes = params.has("windowMinutes") ? params.get("windowMinutes").asInt(60) : 60;

            LocalDateTime windowStart = transaction.getTimestamp().minusMinutes(windowMinutes);
            long count = transactionRepository.countByAccountIdAndTimestampBetween(
                    transaction.getAccountId(), windowStart, transaction.getTimestamp());

            boolean triggered = count >= maxCount;
            if (triggered) {
                log.info("FrequencyRule triggered: account={}, count={} in {}min window",
                        transaction.getAccountId(), count, windowMinutes);
            }
            return triggered;
        } catch (Exception e) {
            log.warn("FrequencyRule parse error for rule {}: {}", rule.getId(), e.getMessage());
            return false;
        }
    }

    @Override
    public FraudRule.RuleType getSupportedType() {
        return FraudRule.RuleType.FREQUENCY;
    }
}
