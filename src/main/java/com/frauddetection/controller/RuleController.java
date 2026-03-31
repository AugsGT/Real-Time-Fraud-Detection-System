package com.frauddetection.controller;

import com.frauddetection.dto.RuleRequest;
import com.frauddetection.model.FraudRule;
import com.frauddetection.repository.FraudRuleRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rules")
@CrossOrigin("*")
public class RuleController {

    private final FraudRuleRepository fraudRuleRepository;

    public RuleController(FraudRuleRepository fraudRuleRepository) {
        this.fraudRuleRepository = fraudRuleRepository;
    }

    @GetMapping
    public List<FraudRule> getAllRules() {
        return fraudRuleRepository.findAll();
    }

    @PostMapping
    public FraudRule createRule(@Valid @RequestBody RuleRequest request) {
        FraudRule rule = new FraudRule();
        rule.setName(request.getName());
        rule.setDescription(request.getDescription());
        rule.setRuleType(request.getRuleType());
        rule.setParamJson(request.getParamJson());
        rule.setEnabled(request.getEnabled());
        rule.setCreatedAt(LocalDateTime.now());
        
        return fraudRuleRepository.save(rule);
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<FraudRule> toggleRule(@PathVariable Long id, @RequestParam boolean enabled) {
        FraudRule rule = fraudRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found"));
        rule.setEnabled(enabled);
        rule.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(fraudRuleRepository.save(rule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        fraudRuleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
