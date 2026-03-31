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
public class ChannelRiskRule implements RuleEngine {

    private static final Logger log = LoggerFactory.getLogger(ChannelRiskRule.class);
    private final ObjectMapper objectMapper;

    public ChannelRiskRule(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean evaluate(Transaction transaction, FraudRule rule) {
        try {
            JsonNode params = objectMapper.readTree(rule.getParamJson());
            double onlineThreshold = params.has("onlineThreshold") ? params.get("onlineThreshold").asDouble(2000.0) : 2000.0;

            if (transaction.getChannel() == Transaction.Channel.ONLINE) {
                boolean triggered = transaction.getAmount().compareTo(BigDecimal.valueOf(onlineThreshold)) > 0;
                if (triggered) {
                    log.info("ChannelRiskRule triggered: account={}, online amount={}",
                            transaction.getAccountId(), transaction.getAmount());
                }
                return triggered;
            }
            return false;
        } catch (Exception e) {
            log.warn("ChannelRiskRule parse error for rule {}: {}", rule.getId(), e.getMessage());
            return false;
        }
    }

    @Override
    public FraudRule.RuleType getSupportedType() {
        return FraudRule.RuleType.CHANNEL_RISK;
    }
}
