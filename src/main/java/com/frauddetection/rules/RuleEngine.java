package com.frauddetection.rules;

import com.frauddetection.model.FraudRule;
import com.frauddetection.model.Transaction;

public interface RuleEngine {
    boolean evaluate(Transaction transaction, FraudRule rule);
    FraudRule.RuleType getSupportedType();
}
