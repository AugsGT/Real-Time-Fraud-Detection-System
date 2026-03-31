package com.frauddetection.dto;

import com.frauddetection.model.FraudRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RuleRequest {

    @NotBlank(message = "Rule name is required")
    private String name;

    private String description;

    @NotNull(message = "Rule type is required")
    private FraudRule.RuleType ruleType;

    private Boolean enabled = true;

    private String paramJson;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public FraudRule.RuleType getRuleType() { return ruleType; }
    public void setRuleType(FraudRule.RuleType ruleType) { this.ruleType = ruleType; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getParamJson() { return paramJson; }
    public void setParamJson(String paramJson) { this.paramJson = paramJson; }
}
