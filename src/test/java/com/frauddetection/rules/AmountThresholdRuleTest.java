package com.frauddetection.rules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frauddetection.model.FraudRule;
import com.frauddetection.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AmountThresholdRuleTest {

    private AmountThresholdRule rule;
    private FraudRule fraudRule;

    @BeforeEach
    void setUp() {
        rule = new AmountThresholdRule(new ObjectMapper());
        
        fraudRule = new FraudRule();
        fraudRule.setId(1L);
        fraudRule.setName("Test Amount Rule");
        fraudRule.setRuleType(FraudRule.RuleType.AMOUNT_THRESHOLD);
        fraudRule.setParamJson("{\"maxAmount\": 5000.0}");
        fraudRule.setEnabled(true);
    }

    private Transaction tx(double amount) {
        Transaction t = new Transaction();
        t.setId(1L);
        t.setAccountId("ACC001");
        t.setAmount(BigDecimal.valueOf(amount));
        t.setChannel(Transaction.Channel.ONLINE);
        t.setTimestamp(LocalDateTime.now());
        return t;
    }

    @Test
    void shouldNotFlagBelowThreshold() {
        assertThat(rule.evaluate(tx(1000.0), fraudRule)).isFalse();
    }

    @Test
    void shouldFlagAboveThreshold() {
        assertThat(rule.evaluate(tx(6000.0), fraudRule)).isTrue();
    }

    @Test
    void shouldFlagExactlyAtThreshold() {
        assertThat(rule.evaluate(tx(5001.0), fraudRule)).isTrue();
    }

    @Test
    void shouldHandleMalformedParams() {
        fraudRule.setParamJson("invalid-json");
        // Should fall back to default threshold (5000)
        assertThat(rule.evaluate(tx(10000.0), fraudRule)).isTrue();
    }

    @Test
    void shouldReturnCorrectType() {
        assertThat(rule.getSupportedType()).isEqualTo(FraudRule.RuleType.AMOUNT_THRESHOLD);
    }
}
