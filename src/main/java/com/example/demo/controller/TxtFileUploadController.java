package com.example.demo.controller;

import com.example.demo.service.TextProcessingService;
import org.jfree.chart.JFreeChart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import javax.imageio.ImageIO;

@Controller
public class TxtFileUploadController {

    @Autowired
    private TextProcessingService textProcessingService;
    private String lastUploadedText = "";

    // Directorio donde se guardarán los archivos
    private static final String UPLOAD_DIR = "uploads";

    @PostMapping("/uploadTxt")
    public ResponseEntity<?> handleTxtFileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }

        if (!"text/plain".equals(file.getContentType())) {
            return ResponseEntity.badRequest().body("Only .txt files are allowed.");
        }

        try {
            // Crear directorio si no existe
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Guardar archivo (se reemplaza si existe uno con el mismo nombre)
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

            lastUploadedText = content.toString();

            // Redirección automática
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/histograma")
                    .build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload or process file: " + e.getMessage());
        }
    }

    @GetMapping(value = "/histograma", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getHistogramChart() {
        if (lastUploadedText.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        try {
            Map<String, Integer> wordFrequencies = textProcessingService.countWordFrequencies(lastUploadedText);
            JFreeChart chart = textProcessingService.createHistogramChart(wordFrequencies);

            // Tamaño aumentado (Full HD: 1920x1080)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedImage image = chart.createBufferedImage(1600, 900);
            ImageIO.write(image, "png", baos);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "inline; filename=histograma.png")
                    .body(baos.toByteArray());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}