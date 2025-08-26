package com.example.demo.controller;

import com.example.demo.service.TextProcessingService;
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

    // Directorio donde se guardarán los archivos
    private static final String UPLOAD_DIR = "uploads";

    @PostMapping("/uploadTxt")
    public String handleTxtFileUpload(@RequestParam("file") MultipartFile file,
                                      @RequestParam("analysisType") String analysisType,
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
            // Crear directorio si no existe
            File uploadDir = new File(UPLOAD_DIR);

            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Guardar archivo
            String fileName = file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.write(filePath, file.getBytes());

            // Leer contenido del archivo
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filePath.toFile()), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }

            String lastUploadedText = content.toString();
            // Valor por defecto

            // Procesar según el tipo de análisis
            Map<String, Integer> frequencies;
            String analysisName = switch (analysisType) {
                case "bigram" -> {
                    frequencies = textProcessingService.countBigramFrequencies(lastUploadedText);
                    yield "Bigramas";
                }
                case "trigram" -> {
                    frequencies = textProcessingService.countTrigramFrequencies(lastUploadedText);
                    yield "Trigramas";
                }
                default -> {
                    frequencies = textProcessingService.countWordFrequencies(lastUploadedText);
                    yield "Unigramas";
                }
            };

            // Ordenar por frecuencia descendente
            Map<String, Integer> sortedFrequencies = frequencies.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));

            // Generar el histograma y convertirlo a base64
            String chartTitle = "Histograma de " + analysisName;
            JFreeChart chart = textProcessingService.createHistogramChart(frequencies, chartTitle);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedImage image = chart.createBufferedImage(1200, 700);
            ImageIO.write(image, "png", baos);
            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());

            // Agregar datos al modelo
            model.addAttribute("frequencies", sortedFrequencies);
            model.addAttribute("analysisType", analysisType);
            model.addAttribute("analysisName", analysisName);
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