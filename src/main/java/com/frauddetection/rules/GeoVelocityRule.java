package com.frauddetection.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frauddetection.model.FraudRule;
import com.frauddetection.model.Transaction;
import com.frauddetection.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeoVelocityRule implements RuleEngine {

    private static final Logger log = LoggerFactory.getLogger(GeoVelocityRule.class);
    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;
    private static final double EARTH_RADIUS_KM = 6371.0;

    public GeoVelocityRule(TransactionRepository transactionRepository, ObjectMapper objectMapper) {
        this.transactionRepository = transactionRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean evaluate(Transaction transaction, FraudRule rule) {
        if (transaction.getLatitude() == null || transaction.getLongitude() == null) {
            return false;
        }
        try {
            JsonNode params = objectMapper.readTree(rule.getParamJson());
            double maxSpeedKmH = params.has("maxSpeedKmH") ? params.get("maxSpeedKmH").asDouble(900.0) : 900.0;

            List<Transaction> previousTxs = transactionRepository.findLatestByAccount(
                    transaction.getAccountId(), PageRequest.of(0, 1));

            if (previousTxs.isEmpty()) return false;

            Transaction prev = previousTxs.get(0);
            if (prev.getLatitude() == null || prev.getLongitude() == null) return false;

            double distanceKm = haversineDistance(prev.getLatitude(), prev.getLongitude(),
                    transaction.getLatitude(), transaction.getLongitude());
            long seconds = java.time.Duration.between(prev.getTimestamp(), transaction.getTimestamp()).abs().getSeconds();

            if (seconds == 0) return distanceKm > 1.0;

            double speedKmH = (distanceKm / seconds) * 3600.0;
            boolean triggered = speedKmH > maxSpeedKmH && distanceKm > 50.0;
            if (triggered) {
                log.info("GeoVelocityRule triggered: account={}, speed={}km/h, distance={}km",
                        transaction.getAccountId(), String.format("%.1f", speedKmH), String.format("%.1f", distanceKm));
            }
            return triggered;
        } catch (Exception e) {
            log.warn("GeoVelocityRule error for rule {}: {}", rule.getId(), e.getMessage());
            return false;
        }
    }

    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    @Override
    public FraudRule.RuleType getSupportedType() {
        return FraudRule.RuleType.GEO_VELOCITY;
    }
}
