package com.example.demo.service;

import java.util.Map;

public class NgramResult {
    private Map<String, Integer> frequencies;
    private int effectiveN;

    public NgramResult(Map<String, Integer> frequencies, int effectiveN) {
        this.frequencies = frequencies;
        this.effectiveN = effectiveN;
    }

    public Map<String, Integer> getFrequencies() {
        return frequencies;
    }

    public int getEffectiveN() {
        return effectiveN;
    }
}