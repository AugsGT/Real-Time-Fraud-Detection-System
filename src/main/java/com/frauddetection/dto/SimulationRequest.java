package com.frauddetection.dto;

public class SimulationRequest {
    private int count = 50;
    private double fraudRatio = 0.2;
    private String targetAccountId;

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public double getFraudRatio() { return fraudRatio; }
    public void setFraudRatio(double fraudRatio) { this.fraudRatio = fraudRatio; }
    public String getTargetAccountId() { return targetAccountId; }
    public void setTargetAccountId(String targetAccountId) { this.targetAccountId = targetAccountId; }
}
