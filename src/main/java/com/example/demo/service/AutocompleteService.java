package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AutocompleteService {

    public static class SuggestionResult {
        private String bestSuggestion;
        private List<Map<String, Object>> topSuggestions;

        public SuggestionResult(String bestSuggestion, List<Map<String, Object>> topSuggestions) {
            this.bestSuggestion = bestSuggestion;
            this.topSuggestions = topSuggestions;
        }

        public String getBestSuggestion() { return bestSuggestion; }
        public List<Map<String, Object>> getTopSuggestions() { return topSuggestions; }
    }

    // 🔥 Eliminado 'frequencies' del método
    public SuggestionResult suggestNextWord(
            String partialText,
            Map<String, Double> probabilities, // ← solo probabilidades
            int ngramSize,
            boolean withBoundaries,
            TextProcessingService textProcessingService) {

        if (partialText == null || partialText.trim().isEmpty()) {
            throw new IllegalArgumentException("El texto parcial no puede estar vacío.");
        }

        List<String> inputTokens = Arrays.stream(partialText.trim().split("\\s+"))
                .map(word -> {
                    if ("<s>".equals(word) || "</s>".equals(word)) {
                        return word;
                    }
                    String normalized = textProcessingService.normalizeWord(word);
                    return TextProcessingService.STOP_WORDS.contains(normalized) ? null : normalized;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (inputTokens.isEmpty()) {
            throw new IllegalArgumentException("El texto parcial no contiene palabras válidas después de la normalización.");
        }

        if (ngramSize == 1) {
            String best = probabilities.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");
            List<Map<String, Object>> top = new ArrayList<>();
            Map<String, Object> sug = new HashMap<>();
            sug.put("word", best);
            sug.put("probability", probabilities.get(best));
            top.add(sug);
            return new SuggestionResult(best, top);
        }

        int contextLen = ngramSize - 1;
        List<String> contextList;
        if (inputTokens.size() >= contextLen) {
            contextList = inputTokens.subList(inputTokens.size() - contextLen, inputTokens.size());
        } else {
            contextList = new ArrayList<>();
            if (withBoundaries) {
                for (int i = 0; i < contextLen - inputTokens.size(); i++) {
                    contextList.add("<s>");
                }
            }
            contextList.addAll(inputTokens);
        }
        String context = String.join(" ", contextList);

        Map<String, Double> candidates = new HashMap<>();
        for (String ngram : probabilities.keySet()) {
            String[] parts = ngram.split(" ");
            if (parts.length == ngramSize) {
                String ngramContext = String.join(" ", Arrays.copyOf(parts, ngramSize - 1));
                if (ngramContext.equals(context)) {
                    String nextWord = parts[ngramSize - 1];
                    candidates.put(nextWord, probabilities.get(ngram));
                }
            }
        }

        if (candidates.isEmpty()) {
            throw new IllegalStateException("No se encontraron sugerencias para el contexto: \"" + context + "\"");
        }

        List<Map<String, Object>> top3 = candidates.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .map(entry -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("word", entry.getKey());
                    m.put("probability", entry.getValue());
                    return m;
                })
                .collect(Collectors.toList());

        String best = top3.get(0).get("word").toString();
        return new SuggestionResult(best, top3);
    }
}