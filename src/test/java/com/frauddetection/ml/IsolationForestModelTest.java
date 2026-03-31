package com.frauddetection.ml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class IsolationForestModelTest {

    private IsolationForestModel model;

    @BeforeEach
    void setUp() {
        model = new IsolationForestModel();
    }

    @Test
    void modelShouldNotBeTrainedInitially() {
        assertThat(model.isTrained()).isFalse();
    }

    @Test
    void modelShouldTrainSuccessfully() {
        model.train(generateNormalData(300));
        assertThat(model.isTrained()).isTrue();
    }

    @Test
    void normalDataShouldScoreLower() {
        model.train(generateNormalData(300));
        double[] normal = {0.05, 0.1, 0.02, 0.05, 0.2};
        double score = model.score(normal);
        assertThat(score).isBetween(0.0, 1.0);
    }

    @Test
    void anomalousDataShouldScoreHigher() {
        model.train(generateNormalData(300));
        double[] normal = {0.05, 0.10, 0.02, 0.05, 0.20};
        double[] anomaly = {1.00, 1.00, 1.00, 1.00, 1.00};
        double normalScore = model.score(normal);
        double anomalyScore = model.score(anomaly);
        assertThat(anomalyScore).isGreaterThan(normalScore);
    }

    @Test
    void scoreWithoutTrainingShouldUseHeuristic() {
        double[] highRisk = {1.0, 1.0, 1.0, 1.0, 0.8};
        double score = model.score(highRisk);
        assertThat(score).isGreaterThan(0.5);
    }

    @Test
    void scoreShouldBeBoundedInRange() {
        model.train(generateNormalData(300));
        Random rng = new Random();
        for (int i = 0; i < 50; i++) {
            double[] features = new double[5];
            for (int j = 0; j < 5; j++) features[j] = rng.nextDouble();
            double score = model.score(features);
            assertThat(score).isBetween(0.0, 1.0);
        }
    }

    private List<double[]> generateNormalData(int n) {
        Random rng = new Random(42);
        List<double[]> data = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            data.add(new double[]{
                    rng.nextDouble() * 0.2,
                    rng.nextDouble() * 0.3,
                    rng.nextDouble() * 0.05,
                    rng.nextDouble() * 0.15,
                    rng.nextDouble() * 0.5
            });
        }
        return data;
    }
}
