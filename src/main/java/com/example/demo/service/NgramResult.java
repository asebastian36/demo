package com.example.demo.service;

import java.util.Map;

public class NgramResult {
    private Map<String, Integer> frequencies;
    private Map<String, Double> probabilities; // Nueva: probabilidades MLE
    private int effectiveN;
    private boolean withBoundaries; // Indica si se usaron <s> y </s>

    public NgramResult(Map<String, Integer> frequencies, Map<String, Double> probabilities, int effectiveN, boolean withBoundaries) {
        this.frequencies = frequencies;
        this.probabilities = probabilities;
        this.effectiveN = effectiveN;
        this.withBoundaries = withBoundaries;
    }

    public Map<String, Integer> getFrequencies() {
        return frequencies;
    }

    public Map<String, Double> getProbabilities() {
        return probabilities;
    }

    public int getEffectiveN() {
        return effectiveN;
    }

    public boolean isWithBoundaries() {
        return withBoundaries;
    }
}