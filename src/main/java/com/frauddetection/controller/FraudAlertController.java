package com.frauddetection.controller;

import com.frauddetection.dto.AlertStatsResponse;
import com.frauddetection.dto.FraudAlertResponse;
import com.frauddetection.model.FraudAlert;
import com.frauddetection.repository.FraudAlertRepository;
import com.frauddetection.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin("*")
public class FraudAlertController {

    private final FraudAlertRepository fraudAlertRepository;
    private final TransactionRepository transactionRepository;

    public FraudAlertController(FraudAlertRepository fraudAlertRepository, TransactionRepository transactionRepository) {
        this.fraudAlertRepository = fraudAlertRepository;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping
    public ResponseEntity<Page<FraudAlertResponse>> getAlerts(
            @RequestParam(required = false) Boolean resolved,
            @RequestParam(required = false) FraudAlert.Severity severity,
            Pageable pageable) {

        Page<FraudAlert> alerts;
        if (resolved != null && severity != null) {
            alerts = fraudAlertRepository.findByResolvedAndSeverity(resolved, severity, pageable);
        } else if (resolved != null) {
            alerts = fraudAlertRepository.findByResolved(resolved, pageable);
        } else if (severity != null) {
            alerts = fraudAlertRepository.findBySeverity(severity, pageable);
        } else {
            alerts = fraudAlertRepository.findAll(pageable);
        }

        return ResponseEntity.ok(alerts.map(this::toResponse));
    }

    @GetMapping("/stats")
    public ResponseEntity<AlertStatsResponse> getStats() {
        AlertStatsResponse res = new AlertStatsResponse();
        
        long totalTx = transactionRepository.count();
        long totalAlerts = fraudAlertRepository.count();

        res.setTotalTransactions(totalTx);
        res.setTotalAlerts(totalAlerts);
        res.setUnresolvedAlerts(fraudAlertRepository.countByResolved(false));
        res.setCriticalAlerts(fraudAlertRepository.countBySeverity(FraudAlert.Severity.CRITICAL));
        res.setHighAlerts(fraudAlertRepository.countBySeverity(FraudAlert.Severity.HIGH));
        res.setMediumAlerts(fraudAlertRepository.countBySeverity(FraudAlert.Severity.MEDIUM));
        res.setLowAlerts(fraudAlertRepository.countBySeverity(FraudAlert.Severity.LOW));
        res.setDetectionRate(totalTx > 0 ? (double) totalAlerts / totalTx : 0.0);
        res.setAlertsLast24h(fraudAlertRepository.countSince(LocalDateTime.now().minusHours(24)));
        res.setAvgAnomalyScore(fraudAlertRepository.avgAnomalyScore());

        Map<String, Long> types = new HashMap<>();
        for (FraudAlert.AlertType type : FraudAlert.AlertType.values()) {
            types.put(type.name(), fraudAlertRepository.countByAlertType(type));
        }
        res.setAlertsByType(types);

        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<FraudAlertResponse> resolveAlert(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        FraudAlert alert = fraudAlertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        alert.setResolved(true);
        if (body != null && body.containsKey("notes")) {
            alert.setNotes(body.get("notes"));
        }

        return ResponseEntity.ok(toResponse(fraudAlertRepository.save(alert)));
    }

    private FraudAlertResponse toResponse(FraudAlert alert) {
        FraudAlertResponse res = new FraudAlertResponse();
        res.setId(alert.getId());
        if (alert.getTransaction() != null) {
            res.setTransactionId(alert.getTransaction().getId());
            res.setAccountId(alert.getTransaction().getAccountId());
            res.setTransactionAmount(alert.getTransaction().getAmount().doubleValue());
            res.setCurrency(alert.getTransaction().getCurrency());
            res.setMerchantId(alert.getTransaction().getMerchantId());
            res.setTransactionTimestamp(alert.getTransaction().getTimestamp());
        }
        res.setAlertType(alert.getAlertType());
        res.setTriggeredRules(alert.getTriggeredRules());
        res.setAnomalyScore(alert.getAnomalyScore());
        res.setSeverity(alert.getSeverity());
        res.setCreatedAt(alert.getCreatedAt());
        res.setResolved(alert.getResolved());
        res.setNotes(alert.getNotes());
        return res;
    }
}
