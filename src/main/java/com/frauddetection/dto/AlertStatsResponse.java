package com.frauddetection.dto;

import java.util.Map;

public class AlertStatsResponse {
    private long totalTransactions;
    private long totalAlerts;
    private long unresolvedAlerts;
    private long criticalAlerts;
    private long highAlerts;
    private long mediumAlerts;
    private long lowAlerts;
    private double detectionRate;
    private Double avgAnomalyScore;
    private long alertsLast24h;
    private Map<String, Long> alertsByType;

    public long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; }
    public long getTotalAlerts() { return totalAlerts; }
    public void setTotalAlerts(long totalAlerts) { this.totalAlerts = totalAlerts; }
    public long getUnresolvedAlerts() { return unresolvedAlerts; }
    public void setUnresolvedAlerts(long unresolvedAlerts) { this.unresolvedAlerts = unresolvedAlerts; }
    public long getCriticalAlerts() { return criticalAlerts; }
    public void setCriticalAlerts(long criticalAlerts) { this.criticalAlerts = criticalAlerts; }
    public long getHighAlerts() { return highAlerts; }
    public void setHighAlerts(long highAlerts) { this.highAlerts = highAlerts; }
    public long getMediumAlerts() { return mediumAlerts; }
    public void setMediumAlerts(long mediumAlerts) { this.mediumAlerts = mediumAlerts; }
    public long getLowAlerts() { return lowAlerts; }
    public void setLowAlerts(long lowAlerts) { this.lowAlerts = lowAlerts; }
    public double getDetectionRate() { return detectionRate; }
    public void setDetectionRate(double detectionRate) { this.detectionRate = detectionRate; }
    public Double getAvgAnomalyScore() { return avgAnomalyScore; }
    public void setAvgAnomalyScore(Double avgAnomalyScore) { this.avgAnomalyScore = avgAnomalyScore; }
    public long getAlertsLast24h() { return alertsLast24h; }
    public void setAlertsLast24h(long alertsLast24h) { this.alertsLast24h = alertsLast24h; }
    public Map<String, Long> getAlertsByType() { return alertsByType; }
    public void setAlertsByType(Map<String, Long> alertsByType) { this.alertsByType = alertsByType; }
}
