package com.frauddetection.ml;

import com.frauddetection.model.Transaction;
import com.frauddetection.repository.TransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class FeatureExtractor {

    private final TransactionRepository transactionRepository;

    public static final int F_AMOUNT_ZSCORE = 0;
    public static final int F_TIME_SINCE_LAST = 1;
    public static final int F_GEO_DISTANCE = 2;
    public static final int F_VELOCITY_1H = 3;
    public static final int F_CHANNEL_RISK = 4;
    public static final int FEATURE_COUNT = 5;
    private static final double EARTH_RADIUS_KM = 6371.0;

    public FeatureExtractor(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public double[] extract(Transaction transaction) {
        double[] features = new double[FEATURE_COUNT];
        features[F_AMOUNT_ZSCORE] = computeAmountZScore(transaction);
        features[F_TIME_SINCE_LAST] = computeTimeSinceLast(transaction);
        features[F_GEO_DISTANCE] = computeGeoDistance(transaction);
        features[F_VELOCITY_1H] = computeVelocity1h(transaction);
        features[F_CHANNEL_RISK] = computeChannelRisk(transaction);
        return features;
    }

    private double computeAmountZScore(Transaction tx) {
        double amount = tx.getAmount().doubleValue();
        double zScore = (amount - 200.0) / 500.0;
        return Math.min(Math.max(zScore, 0.0), 3.0) / 3.0;
    }

    private double computeTimeSinceLast(Transaction tx) {
        List<Transaction> recent = transactionRepository.findLatestByAccount(
                tx.getAccountId(), PageRequest.of(0, 1));
        if (recent.isEmpty()) return 0.5;
        long minutes = Duration.between(recent.get(0).getTimestamp(), tx.getTimestamp()).abs().toMinutes();
        if (minutes < 2) return 1.0;
        return Math.max(0.0, 1.0 - (minutes / 1440.0));
    }

    private double computeGeoDistance(Transaction tx) {
        if (tx.getLatitude() == null || tx.getLongitude() == null) return 0.0;
        List<Transaction> recent = transactionRepository.findLatestByAccount(
                tx.getAccountId(), PageRequest.of(0, 1));
        if (recent.isEmpty()) return 0.0;
        Transaction prev = recent.get(0);
        if (prev.getLatitude() == null || prev.getLongitude() == null) return 0.0;
        double dist = haversine(prev.getLatitude(), prev.getLongitude(), tx.getLatitude(), tx.getLongitude());
        return Math.min(dist / 20000.0, 1.0);
    }

    private double computeVelocity1h(Transaction tx) {
        LocalDateTimeMinusHour since = new LocalDateTimeMinusHour(tx.getTimestamp().minusHours(1));
        long count = transactionRepository.countByAccountIdAndTimestampBetween(
                tx.getAccountId(), since.value, tx.getTimestamp());
        return Math.min(count / 20.0, 1.0);
    }

    private double computeChannelRisk(Transaction tx) {
        if (tx.getChannel() == null) return 0.3;
        return switch (tx.getChannel()) {
            case ONLINE -> 0.8;
            case MOBILE -> 0.5;
            case ATM -> 0.4;
            case POS -> 0.2;
        };
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private static class LocalDateTimeMinusHour {
        final java.time.LocalDateTime value;
        LocalDateTimeMinusHour(java.time.LocalDateTime v) { this.value = v; }
    }
}
