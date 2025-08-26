package com.example.demo.service;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.*;

@Service
public class TextProcessingService {

    // Lista de palabras a excluir (conectores, artículos, etc.)
    private static final Set<String> STOP_WORDS = Set.of(
            "el", "la", "los", "las", "un", "una", "unos", "unas",
            "de", "del", "a", "al", "y", "o", "pero", "se", "que",
            "por", "con", "este", "esta", "estos"
    );

    public Map<String, Integer> countWordFrequencies(String text) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        List<String> words = extractWords(text);

        for (String word : words) {
            if(!STOP_WORDS.contains(word)) {
                frequencyMap.merge(word, 1, Integer::sum);
            }
        }

        return frequencyMap;
    }

    public Map<String, Integer> countBigramFrequencies(String text) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        List<String> words = extractWords(text);

        // Filtrar palabras cortas y stop words
        List<String> filteredWords = new ArrayList<>();
        for (String word : words) {
            if(!STOP_WORDS.contains(word)) {
                filteredWords.add(word);
            }
        }

        // Generar bigramas
        for (int i = 0; i < filteredWords.size() - 1; i++) {
            String bigram = filteredWords.get(i) + " " + filteredWords.get(i + 1);
            frequencyMap.merge(bigram, 1, Integer::sum);
        }

        return frequencyMap;
    }

    public Map<String, Integer> countTrigramFrequencies(String text) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        List<String> words = extractWords(text);

        // Filtrar palabras cortas y stop words
        List<String> filteredWords = new ArrayList<>();
        for (String word : words) {
            if(!STOP_WORDS.contains(word)) {
                filteredWords.add(word);
            }
        }

        // Generar trigramas
        for (int i = 0; i < filteredWords.size() - 2; i++) {
            String trigram = filteredWords.get(i) + " " + filteredWords.get(i + 1) + " " + filteredWords.get(i + 2);
            frequencyMap.merge(trigram, 1, Integer::sum);
        }

        return frequencyMap;
    }

    private List<String> extractWords(String text) {
        List<String> words = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\p{L}+");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            words.add(matcher.group().toLowerCase());
        }

        return words;
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

        // Aumentar tamaño del gráfico
        chart.getCategoryPlot().getDomainAxis().setMaximumCategoryLabelWidthRatio(0.8f);
        return chart;
    }
}