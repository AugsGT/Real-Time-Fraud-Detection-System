package com.frauddetection.dto;

import com.frauddetection.model.FraudAlert;
import java.time.LocalDateTime;

public class FraudAlertResponse {
    private Long id;
    private Long transactionId;
    private String accountId;
    private Double transactionAmount;
    private String currency;
    private String merchantId;
    private LocalDateTime transactionTimestamp;
    private FraudAlert.AlertType alertType;
    private String triggeredRules;
    private Double anomalyScore;
    private FraudAlert.Severity severity;
    private LocalDateTime createdAt;
    private Boolean resolved;
    private String notes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public Double getTransactionAmount() { return transactionAmount; }
    public void setTransactionAmount(Double transactionAmount) { this.transactionAmount = transactionAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public LocalDateTime getTransactionTimestamp() { return transactionTimestamp; }
    public void setTransactionTimestamp(LocalDateTime transactionTimestamp) { this.transactionTimestamp = transactionTimestamp; }
    public FraudAlert.AlertType getAlertType() { return alertType; }
    public void setAlertType(FraudAlert.AlertType alertType) { this.alertType = alertType; }
    public String getTriggeredRules() { return triggeredRules; }
    public void setTriggeredRules(String triggeredRules) { this.triggeredRules = triggeredRules; }
    public Double getAnomalyScore() { return anomalyScore; }
    public void setAnomalyScore(Double anomalyScore) { this.anomalyScore = anomalyScore; }
    public FraudAlert.Severity getSeverity() { return severity; }
    public void setSeverity(FraudAlert.Severity severity) { this.severity = severity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Boolean getResolved() { return resolved; }
    public void setResolved(Boolean resolved) { this.resolved = resolved; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
