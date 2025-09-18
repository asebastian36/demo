package com.example.demo.service;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.*;
import java.text.Normalizer;

@Service
public class TextProcessingService {

    public static final Set<String> STOP_WORDS = Set.of(
            "el", "la", "los", "las", "un", "una", "unos", "unas",
            "de", "del", "a", "al", "y", "o", "pero", "se", "que",
            "por", "con", "este", "esta", "estos", "en", "es"
    );

    public Map<String, Integer> countWordFrequencies(String text) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        List<String> words = extractWords(text);

        for (String word : words) {
            String normalizedWord = normalizeWord(word);
            if (!STOP_WORDS.contains(normalizedWord)) {
                frequencyMap.merge(normalizedWord, 1, Integer::sum);
            }
        }

        return frequencyMap;
    }

    public List<String> extractSentencesWithBoundaries(String text) {
        List<String> tokens = new ArrayList<>();
        String[] sentences = text.split("[.!?]+");

        for (String sentence : sentences) {
            List<String> words = extractWords(sentence.trim());
            if (!words.isEmpty()) {
                tokens.add("<s>");
                for (String word : words) {
                    String normalized = normalizeWord(word);
                    if (!STOP_WORDS.contains(normalized)) {
                        tokens.add(normalized);
                    }
                }
                tokens.add("</s>");
            }
        }

        return tokens;
    }

    public List<String> extractWords(String text) {
        List<String> words = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\p{L}+");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            words.add(matcher.group());
        }

        return words;
    }

    // 👇 AHORA ES PÚBLICO 👇
    public String normalizeWord(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        String lowerCaseWord = word.toLowerCase();
        String normalized = Normalizer.normalize(lowerCaseWord, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }

    private Map<String, Integer> generateContextFrequencies(List<String> tokens, int n) {
        if (n <= 1) return Map.of();

        Map<String, Integer> contextFreqs = new HashMap<>();
        for (int i = 0; i <= tokens.size() - (n - 1); i++) {
            StringBuilder context = new StringBuilder();
            for (int j = 0; j < n - 1; j++) {
                if (j > 0) context.append(" ");
                context.append(tokens.get(i + j));
            }
            String contextStr = context.toString();
            contextFreqs.merge(contextStr, 1, Integer::sum);
        }
        return contextFreqs;
    }

    private Map<String, Double> calculateProbabilities(Map<String, Integer> ngramFreqs, Map<String, Integer> contextFreqs) {
        Map<String, Double> probabilities = new HashMap<>();
        for (Map.Entry<String, Integer> entry : ngramFreqs.entrySet()) {
            String ngram = entry.getKey();
            int ngramCount = entry.getValue();

            String[] parts = ngram.split(" ");
            if (parts.length <= 1) {
                probabilities.put(ngram, 1.0);
                continue;
            }

            String context = String.join(" ", Arrays.copyOf(parts, parts.length - 1));
            Integer contextCount = contextFreqs.get(context);

            if (contextCount != null && contextCount > 0) {
                double prob = (double) ngramCount / contextCount;
                probabilities.put(ngram, prob);
            } else {
                probabilities.put(ngram, 0.0);
            }
        }
        return probabilities;
    }

    public NgramResult countNgramFrequencies(String text, int n, boolean withBoundaries) {
        List<String> tokens = withBoundaries ? extractSentencesWithBoundaries(text) : extractWords(text);

        List<String> filteredTokens = new ArrayList<>();
        for (String token : tokens) {
            if (token.equals("<s>") || token.equals("</s>")) {
                filteredTokens.add(token);
            } else {
                String normalized = normalizeWord(token);
                if (!STOP_WORDS.contains(normalized)) {
                    filteredTokens.add(normalized);
                }
            }
        }

        int effectiveN = n;
        if (n < 1) effectiveN = 1;
        if (n > filteredTokens.size()) effectiveN = filteredTokens.size();
        if (effectiveN == 1) {
            Map<String, Integer> unigramFreqs = countWordFrequencies(text);
            Map<String, Double> unigramProbs = new HashMap<>();
            int totalWords = unigramFreqs.values().stream().mapToInt(Integer::intValue).sum();
            for (String word : unigramFreqs.keySet()) {
                unigramProbs.put(word, (double) unigramFreqs.get(word) / totalWords);
            }
            return new NgramResult(unigramFreqs, unigramProbs, effectiveN, withBoundaries);
        }

        Map<String, Integer> ngramFreqs = new HashMap<>();
        for (int i = 0; i <= filteredTokens.size() - effectiveN; i++) {
            StringBuilder ngram = new StringBuilder();
            for (int j = 0; j < effectiveN; j++) {
                if (j > 0) ngram.append(" ");
                ngram.append(filteredTokens.get(i + j));
            }
            String ngramStr = ngram.toString();
            ngramFreqs.merge(ngramStr, 1, Integer::sum);
        }

        Map<String, Integer> contextFreqs = generateContextFrequencies(filteredTokens, effectiveN);
        Map<String, Double> probabilities = calculateProbabilities(ngramFreqs, contextFreqs);

        return new NgramResult(ngramFreqs, probabilities, effectiveN, withBoundaries);
    }

    public JFreeChart createHistogramChart(Map<String, Integer> frequencies, String title) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        frequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(20)
                .forEach(e -> dataset.addValue(e.getValue(), "Frecuencia", e.getKey()));

        JFreeChart chart = ChartFactory.createBarChart(
                title,
                "Palabra",
                "Frecuencia",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        chart.getCategoryPlot().getDomainAxis().setMaximumCategoryLabelWidthRatio(0.8f);
        return chart;
    }
}