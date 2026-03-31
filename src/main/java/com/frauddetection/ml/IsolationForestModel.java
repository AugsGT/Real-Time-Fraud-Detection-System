package com.frauddetection.ml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class IsolationForestModel {

    private static final Logger log = LoggerFactory.getLogger(IsolationForestModel.class);
    private static final int NUM_TREES = 100;
    private static final int MAX_SAMPLES = 256;
    private static final int MAX_DEPTH = 10;

    private List<Node> trees;
    private boolean trained = false;

    public void train(List<double[]> data) {
        if (data == null || data.isEmpty()) {
            log.warn("IsolationForest: Cannot train on empty data");
            return;
        }

        trees = new ArrayList<>();
        Random random = new Random(42);

        for (int i = 0; i < NUM_TREES; i++) {
            List<double[]> sample = new ArrayList<>();
            for (int j = 0; j < Math.min(MAX_SAMPLES, data.size()); j++) {
                sample.add(data.get(random.nextInt(data.size())));
            }
            trees.add(buildTree(sample, 0, random));
        }

        trained = true;
        log.info("IsolationForest trained with {} trees on {} samples max", NUM_TREES, Math.min(MAX_SAMPLES, data.size()));
    }

    private Node buildTree(List<double[]> X, int currentDepth, Random random) {
        if (currentDepth >= MAX_DEPTH || X.size() <= 1) {
            return new Node(X.size());
        }

        int numFeatures = X.get(0).length;
        int q = random.nextInt(numFeatures);

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (double[] x : X) {
            if (x[q] < min) min = x[q];
            if (x[q] > max) max = x[q];
        }

        if (min == max) return new Node(X.size());

        double p = min + random.nextDouble() * (max - min);

        List<double[]> left = new ArrayList<>();
        List<double[]> right = new ArrayList<>();

        for (double[] x : X) {
            if (x[q] < p) left.add(x); else right.add(x);
        }

        if (left.isEmpty() || right.isEmpty()) return new Node(X.size());

        return new Node(q, p, buildTree(left, currentDepth + 1, random), buildTree(right, currentDepth + 1, random));
    }

    public double score(double[] x) {
        if (!trained) return sumScores(x) / FeatureExtractor.FEATURE_COUNT;

        double expectedPathLength = c(Math.min(MAX_SAMPLES, trees.get(0).size));
        double avgPathLength = 0;

        for (Node tree : trees) {
            avgPathLength += pathLength(x, tree, 0);
        }
        avgPathLength /= NUM_TREES;

        return Math.pow(2.0, -avgPathLength / expectedPathLength);
    }

    private double sumScores(double[] features) {
        double sum = 0;
        for (double f : features) sum += f;
        return sum;
    }

    private double pathLength(double[] x, Node node, int currentDepth) {
        if (node.isLeaf()) return currentDepth + c(node.size);
        if (x[node.splitFeature] < node.splitValue) {
            return pathLength(x, node.left, currentDepth + 1);
        } else {
            return pathLength(x, node.right, currentDepth + 1);
        }
    }

    private double c(double n) {
        if (n <= 1) return 0;
        return 2.0 * (Math.log(n - 1) + 0.5772156649) - (2.0 * (n - 1) / n);
    }

    public boolean isTrained() {
        return trained;
    }

    private static class Node {
        boolean isLeaf;
        int size;
        int splitFeature;
        double splitValue;
        Node left, right;

        Node(int size) {
            this.isLeaf = true;
            this.size = size;
        }

        Node(int feature, double value, Node left, Node right) {
            this.isLeaf = false;
            this.splitFeature = feature;
            this.splitValue = value;
            this.left = left;
            this.right = right;
        }

        boolean isLeaf() { return isLeaf; }
    }
}
