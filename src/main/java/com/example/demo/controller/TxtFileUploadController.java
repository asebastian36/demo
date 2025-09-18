package com.example.demo.controller;

import com.example.demo.service.TextProcessingService;
import com.example.demo.service.NgramResult;
import org.jfree.chart.JFreeChart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

@Controller
public class TxtFileUploadController {

    @Autowired
    private TextProcessingService textProcessingService;

    private static final String UPLOAD_DIR = "uploads";

    @PostMapping("/uploadTxt")
    public String handleTxtFileUpload(@RequestParam("file") MultipartFile file,
                                      @RequestParam("analysisType") String analysisType,
                                      @RequestParam(value = "ngramSize", required = false, defaultValue = "2") Integer ngramSize,
                                      @RequestParam(value = "withBoundaries", required = false, defaultValue = "false") Boolean withBoundaries,
                                      Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Por favor selecciona un archivo para subir.");
            return "index";
        }

        if (!"text/plain".equals(file.getContentType())) {
            model.addAttribute("error", "Solo se permiten archivos .txt.");
            return "index";
        }

        try {
            if ("ngram".equals(analysisType) && (ngramSize < 2)) {
                model.addAttribute("error", "El tamaño del n-grama debe ser mayor o igual a 2");
                return "index";
            }

            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String fileName = file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.write(filePath, file.getBytes());

            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filePath.toFile()), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }

            String lastUploadedText = content.toString();

            Map<String, Integer> frequencies;
            Map<String, Double> probabilities = new HashMap<>();
            String analysisName;
            Integer actualNgramSize = null;
            boolean usedBoundaries = false;

            // Generar corpus tokenizado para mostrar
            List<String> tokensToShow;
            if ("ngram".equals(analysisType)) {
                tokensToShow = withBoundaries ?
                        textProcessingService.extractSentencesWithBoundaries(lastUploadedText).stream()
                                .map(word -> "<s>".equals(word) || "</s>".equals(word) ? word : textProcessingService.normalizeWord(word))
                                .map(word -> TextProcessingService.STOP_WORDS.contains(word) && !"<s>".equals(word) && !"</s>".equals(word) ? null : word)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                        :
                        textProcessingService.extractWords(lastUploadedText).stream()
                                .map(textProcessingService::normalizeWord)
                                .filter(word -> !TextProcessingService.STOP_WORDS.contains(word))
                                .collect(Collectors.toList());
            } else {
                tokensToShow = textProcessingService.extractWords(lastUploadedText).stream()
                        .map(textProcessingService::normalizeWord)
                        .filter(word -> !TextProcessingService.STOP_WORDS.contains(word))
                        .collect(Collectors.toList());
            }

            String tokenizedCorpus = String.join(" ", tokensToShow);
            model.addAttribute("tokenizedCorpus", tokenizedCorpus);

            if ("ngram".equals(analysisType)) {
                NgramResult ngramResult = textProcessingService.countNgramFrequencies(lastUploadedText, ngramSize, withBoundaries);
                frequencies = ngramResult.getFrequencies();
                probabilities = ngramResult.getProbabilities();
                actualNgramSize = ngramResult.getEffectiveN();
                usedBoundaries = ngramResult.isWithBoundaries();
                analysisName = actualNgramSize + "-gramas" + (usedBoundaries ? " (con fronteras)" : "");
            } else {
                frequencies = textProcessingService.countWordFrequencies(lastUploadedText);
                int total = frequencies.values().stream().mapToInt(Integer::intValue).sum();
                for (String word : frequencies.keySet()) {
                    probabilities.put(word, (double) frequencies.get(word) / total);
                }
                analysisName = "Unigramas" + (withBoundaries ? " (con fronteras)" : "");
                actualNgramSize = 1;
                usedBoundaries = withBoundaries;
            }

            Map<String, Integer> sortedFrequencies = frequencies.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));

            String chartTitle = "Histograma de " + analysisName;
            JFreeChart chart = textProcessingService.createHistogramChart(frequencies, chartTitle);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedImage image = chart.createBufferedImage(1200, 700);
            ImageIO.write(image, "png", baos);
            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());

            model.addAttribute("frequencies", sortedFrequencies);
            model.addAttribute("probabilities", probabilities);
            model.addAttribute("analysisType", analysisType);
            model.addAttribute("analysisName", analysisName);
            model.addAttribute("actualNgramSize", actualNgramSize);
            model.addAttribute("requestedNgramSize", ngramSize);
            model.addAttribute("withBoundaries", usedBoundaries);
            model.addAttribute("hasResults", true);
            model.addAttribute("chartImage", base64Image);
            model.addAttribute("chartTitle", chartTitle);
            model.addAttribute("hasChart", true);

            return "index";

        } catch (Exception e) {
            model.addAttribute("error", "Error al procesar el archivo: " + e.getMessage());
            return "index";
        }
    }
}