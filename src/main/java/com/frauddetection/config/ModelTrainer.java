package com.frauddetection.config;

import com.frauddetection.ml.FeatureExtractor;
import com.frauddetection.ml.IsolationForestModel;
import com.frauddetection.model.Transaction;
import com.frauddetection.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class ModelTrainer {

    private static final Logger log = LoggerFactory.getLogger(ModelTrainer.class);

    private final TransactionRepository transactionRepository;
    private final FeatureExtractor featureExtractor;
    private final IsolationForestModel model;

    public ModelTrainer(TransactionRepository transactionRepository, FeatureExtractor featureExtractor, IsolationForestModel model) {
        this.transactionRepository = transactionRepository;
        this.featureExtractor = featureExtractor;
        this.model = model;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void trainModelOnStartup() {
        log.info("Gathering data for initial model training...");
        List<Transaction> historicalData = transactionRepository.findAll();
        List<double[]> trainingSet = new ArrayList<>();

        for (Transaction tx : historicalData) {
            if (tx.getStatus() != Transaction.TransactionStatus.FLAGGED &&
                tx.getStatus() != Transaction.TransactionStatus.BLOCKED) {
                try {
                    trainingSet.add(featureExtractor.extract(tx));
                } catch (Exception e) {
                    log.warn("Skipping tx {} for training: {}", tx.getId(), e.getMessage());
                }
            }
        }

        if (trainingSet.size() < 100) {
            log.info("Not enough historical normal data. Generating synthetic baseline...");
            Random rng = new Random(42);
            for (int i = 0; i < 256; i++) {
                trainingSet.add(new double[]{
                        rng.nextDouble() * 0.1,  // amount
                        0.5 + rng.nextDouble() * 0.5, // time interval (healthy)
                        rng.nextDouble() * 0.05, // geo distance
                        rng.nextDouble() * 0.1,  // velocity
                        rng.nextDouble() * 0.4   // risk
                });
            }
        }

        long start = System.currentTimeMillis();
        model.train(trainingSet);
        log.info("Model training completed in {} ms.", (System.currentTimeMillis() - start));
    }
}
