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
        Pattern pattern = Pattern.compile("\\p{L}+");

        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String word = matcher.group().toLowerCase();
            // Filtramos palabras cortas y conectores
            if(word.length() > 2 && !STOP_WORDS.contains(word)) {
                frequencyMap.merge(word, 1, Integer::sum);
            }
        }

        return frequencyMap;
    }

    public JFreeChart createHistogramChart(Map<String, Integer> wordFrequencies) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        wordFrequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(20)
                .forEach(e -> dataset.addValue(e.getValue(), "Frecuencia", e.getKey()));

        JFreeChart chart = ChartFactory.createBarChart(
                "Histograma de Palabras (Palabras Clave)",
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