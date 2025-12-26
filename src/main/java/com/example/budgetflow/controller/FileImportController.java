package com.example.budgetflow.controller;

import com.example.budgetflow.service.FileImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class FileImportController {

    private final FileImportService fileImportService;

    @PostMapping("/operations")
    public ResponseEntity<FileImportService.ImportResult> importOperations(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long userId,
            @RequestParam String accountNumber) {
        try {
            FileImportService.ImportResult result = fileImportService.importOperationsFile(
                    file, userId, accountNumber);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new FileImportService.ImportResult(false, 0, 0, 
                            java.util.List.of(e.getMessage())));
        }
    }
}

