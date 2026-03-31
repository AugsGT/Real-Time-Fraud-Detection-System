package com.frauddetection.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_rules")
public class FraudRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private RuleType ruleType;

    private Boolean enabled = true;

    @Column(length = 2000)
    private String paramJson;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public FraudRule() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public RuleType getRuleType() { return ruleType; }
    public void setRuleType(RuleType ruleType) { this.ruleType = ruleType; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getParamJson() { return paramJson; }
    public void setParamJson(String paramJson) { this.paramJson = paramJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public enum RuleType {
        AMOUNT_THRESHOLD,
        FREQUENCY,
        GEO_VELOCITY,
        CHANNEL_RISK,
        VELOCITY
    }
}
