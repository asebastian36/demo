package com.example.demo.controller;

import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.charset.StandardCharsets;

@Controller
public class TxtFileUploadController {

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
            // Process the 'content' string (e.g., save to DB, print, etc.)
            System.out.println("Uploaded .txt file content:\n" + content.toString());

            return ResponseEntity.ok("Text file uploaded and processed successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload or process file: " + e.getMessage());
        }
    }
}