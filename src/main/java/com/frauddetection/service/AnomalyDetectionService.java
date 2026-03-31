package com.frauddetection.service;

import com.frauddetection.ml.FeatureExtractor;
import com.frauddetection.ml.IsolationForestModel;
import com.frauddetection.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AnomalyDetectionService {

    private static final Logger log = LoggerFactory.getLogger(AnomalyDetectionService.class);

    private final IsolationForestModel model;
    private final FeatureExtractor featureExtractor;

    @Value("${fraud.detection.anomaly-threshold:0.65}")
    private double anomalyThreshold;

    public AnomalyDetectionService(IsolationForestModel model, FeatureExtractor featureExtractor) {
        this.model = model;
        this.featureExtractor = featureExtractor;
    }

    public AnomalyResult evaluate(Transaction transaction) {
        double[] features = featureExtractor.extract(transaction);
        double score = model.score(features);
        boolean isAnomaly = score >= anomalyThreshold;

        log.debug("Anomaly evaluation: txId={}, score={}, anomaly={}", transaction.getId(), score, isAnomaly);
        return new AnomalyResult(score, isAnomaly);
    }

    public static class AnomalyResult {
        private final double score;
        private final boolean anomaly;

        public AnomalyResult(double score, boolean anomaly) {
            this.score = score;
            this.anomaly = anomaly;
        }

        public double getScore() { return score; }
        public boolean isAnomaly() { return anomaly; }
    }
}
