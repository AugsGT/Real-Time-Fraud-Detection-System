package com.frauddetection.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_alerts")
public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Enumerated(EnumType.STRING)
    private AlertType alertType;

    @Column(length = 1000)
    private String triggeredRules;

    private Double anomalyScore;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private Boolean resolved = false;

    private String notes;

    public FraudAlert() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }
    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }
    public String getTriggeredRules() { return triggeredRules; }
    public void setTriggeredRules(String triggeredRules) { this.triggeredRules = triggeredRules; }
    public Double getAnomalyScore() { return anomalyScore; }
    public void setAnomalyScore(Double anomalyScore) { this.anomalyScore = anomalyScore; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Boolean getResolved() { return resolved; }
    public void setResolved(Boolean resolved) { this.resolved = resolved; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public enum AlertType { RULE, ANOMALY, BOTH }
    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }
}
