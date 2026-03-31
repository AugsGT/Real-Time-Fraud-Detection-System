package com.frauddetection.service;

import com.frauddetection.model.FraudAlert;
import com.frauddetection.model.Transaction;
import com.frauddetection.repository.FraudAlertRepository;
import com.frauddetection.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class FraudDetectionPipeline {

    private static final Logger log = LoggerFactory.getLogger(FraudDetectionPipeline.class);

    private final RuleEvaluationService ruleEvaluationService;
    private final AnomalyDetectionService anomalyDetectionService;
    private final FraudAlertRepository fraudAlertRepository;
    private final TransactionRepository transactionRepository;

    public FraudDetectionPipeline(RuleEvaluationService ruleEvaluationService,
                                  AnomalyDetectionService anomalyDetectionService,
                                  FraudAlertRepository fraudAlertRepository,
                                  TransactionRepository transactionRepository) {
        this.ruleEvaluationService = ruleEvaluationService;
        this.anomalyDetectionService = anomalyDetectionService;
        this.fraudAlertRepository = fraudAlertRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void process(Transaction transaction) {
        log.info("Processing transaction: id={}, account={}, amount={}",
                transaction.getId(), transaction.getAccountId(), transaction.getAmount());

        RuleEvaluationService.RuleEvaluationResult ruleResult = ruleEvaluationService.evaluate(transaction);
        AnomalyDetectionService.AnomalyResult anomalyResult = anomalyDetectionService.evaluate(transaction);

        boolean isFraud = ruleResult.isTriggered() || anomalyResult.isAnomaly();

        if (isFraud) {
            FraudAlert.AlertType alertType = determineAlertType(ruleResult.isTriggered(), anomalyResult.isAnomaly());
            FraudAlert.Severity severity = determineSeverity(ruleResult, anomalyResult);

            FraudAlert alert = new FraudAlert();
            alert.setTransaction(transaction);
            alert.setAlertType(alertType);
            alert.setTriggeredRules(String.join(", ", ruleResult.getTriggeredRules()));
            alert.setAnomalyScore(anomalyResult.getScore());
            alert.setSeverity(severity);
            alert.setCreatedAt(LocalDateTime.now());
            alert.setResolved(false);

            fraudAlertRepository.save(alert);

            transaction.setStatus(severity == FraudAlert.Severity.CRITICAL ?
                    Transaction.TransactionStatus.BLOCKED : Transaction.TransactionStatus.FLAGGED);
            transactionRepository.save(transaction);

            log.info("FRAUD DETECTED: txId={}, type={}, severity={}, score={}, rules={}",
                    transaction.getId(), alertType, severity,
                    String.format("%.3f", anomalyResult.getScore()),
                    ruleResult.getTriggeredRules());
        } else {
            transaction.setStatus(Transaction.TransactionStatus.APPROVED);
            transactionRepository.save(transaction);
        }
    }

    private FraudAlert.AlertType determineAlertType(boolean ruleTriggered, boolean isAnomaly) {
        if (ruleTriggered && isAnomaly) return FraudAlert.AlertType.BOTH;
        if (ruleTriggered) return FraudAlert.AlertType.RULE;
        return FraudAlert.AlertType.ANOMALY;
    }

    private FraudAlert.Severity determineSeverity(
            RuleEvaluationService.RuleEvaluationResult ruleResult,
            AnomalyDetectionService.AnomalyResult anomalyResult) {

        int ruleScore = ruleResult.getTriggeredRules().size();
        double anomalyScore = anomalyResult.getScore();

        if (ruleScore >= 2 && anomalyScore >= 0.80) return FraudAlert.Severity.CRITICAL;
        if (ruleScore >= 1 && anomalyScore >= 0.70) return FraudAlert.Severity.HIGH;
        if (ruleScore >= 1 || anomalyScore >= 0.65) return FraudAlert.Severity.MEDIUM;
        return FraudAlert.Severity.LOW;
    }
}
