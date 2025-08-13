package com.example.demo.controller;

import org.jfree.chart.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

@Controller
public class TxtFileUploadController {

    private String lastUploadedText = ""; // Guardar el último texto subido

    @GetMapping(value = { "/", ""})
    public String index() {
        return "index";
    }

    @PostMapping("/uploadTxt")
    public ResponseEntity<String> handleTxtFileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }

        if (!"text/plain".equals(file.getContentType())) {
            return ResponseEntity.badRequest().body("Only .txt files are allowed.");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            lastUploadedText = content.toString(); // Guardamos el texto para graficar luego

            return ResponseEntity.ok("File uploaded! You can now see the histogram at /histograma");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload or process file: " + e.getMessage());
        }
    }

    @GetMapping(value = "/histograma", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getHistogramChart() {
        if (lastUploadedText.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        // Contar palabras
        Map<String, Integer> frecuenciaPalabras = new HashMap<>();
        Pattern pattern = Pattern.compile("[\\p{L}]+");

        Matcher matcher = pattern.matcher(lastUploadedText);
        while (matcher.find()) {
            String palabra = matcher.group().toLowerCase();
            frecuenciaPalabras.merge(palabra, 1, Integer::sum);
        }

        // Dataset para JFreeChart
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        frecuenciaPalabras.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(20)
                .forEach(e -> dataset.addValue(e.getValue(), "Frecuencia", e.getKey()));

        // Crear gráfico
        JFreeChart chart = ChartFactory.createBarChart(
                "Histograma de Palabras",
                "Palabra",
                "Frecuencia",
                dataset
        );

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            BufferedImage image = chart.createBufferedImage(800, 600);
            ImageIO.write(image, "png", baos);
            return ResponseEntity.ok(baos.toByteArray());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
