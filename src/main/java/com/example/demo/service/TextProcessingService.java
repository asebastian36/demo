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

    // Lista de palabras a excluir (conectores, artículos, etc.)
    private static final Set<String> STOP_WORDS = Set.of(
            "el", "la", "los", "las", "un", "una", "unos", "unas",
            "de", "del", "a", "al", "y", "o", "pero", "se", "que",
            "por", "con", "este", "esta", "estos", "en", "es"
    );

    public Map<String, Integer> countWordFrequencies(String text) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        List<String> words = extractWords(text);

        for (String word : words) {
            String normalizedWord = normalizeWord(word);
            if(!STOP_WORDS.contains(normalizedWord)) {
                frequencyMap.merge(normalizedWord, 1, Integer::sum);
            }
        }

        return frequencyMap;
    }

    public NgramResult countNgramFrequencies(String text, int n) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        List<String> words = extractWords(text);

        // Filtrar stop words
        List<String> filteredWords = new ArrayList<>();
        for (String word : words) {
            String normalizedWord = normalizeWord(word);
            if(!STOP_WORDS.contains(normalizedWord)) {
                filteredWords.add(normalizedWord);
            }
        }

        // Validar y ajustar el tamaño del n-grama
        int effectiveN = n;
        if (n < 1) {
            effectiveN = 1; // Mínimo 1-grama (unigrama)
        } else if (n > filteredWords.size()) {
            effectiveN = filteredWords.size(); // Ajustar al máximo posible
        }

        // Si effectiveN es 1, usar el método de unigramas para consistencia
        if (effectiveN == 1) {
            return new NgramResult(countWordFrequencies(text), effectiveN);
        }

        // Generar n-gramas
        for (int i = 0; i <= filteredWords.size() - effectiveN; i++) {
            StringBuilder ngramBuilder = new StringBuilder();
            for (int j = 0; j < effectiveN; j++) {
                if (j > 0) {
                    ngramBuilder.append(" ");
                }
                ngramBuilder.append(filteredWords.get(i + j));
            }
            String ngram = ngramBuilder.toString();
            frequencyMap.merge(ngram, 1, Integer::sum);
        }

        return new NgramResult(frequencyMap, effectiveN);
    }

    private List<String> extractWords(String text) {
        List<String> words = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\p{L}+");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            words.add(matcher.group());
        }

        return words;
    }

    /**
     * Normaliza una palabra: convierte a minúsculas y elimina acentos
     * @param word Palabra a normalizar
     * @return Palabra normalizada en minúsculas y sin acentos
     */
    private String normalizeWord(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }

        // Convertir a minúsculas
        String lowerCaseWord = word.toLowerCase();

        // Eliminar acentos y diacríticos
        String normalized = Normalizer.normalize(lowerCaseWord, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

        return pattern.matcher(normalized).replaceAll("");
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