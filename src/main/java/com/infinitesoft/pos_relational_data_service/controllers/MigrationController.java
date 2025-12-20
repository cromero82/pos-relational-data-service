package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.services.MigrationResult;
import com.infinitesoft.pos_relational_data_service.services.MigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/migrations")
@CrossOrigin(origins = "*")
public class MigrationController {

    @Autowired
    private MigrationService migrationService;

    @PostMapping(path = "/ventasya", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MigrationResult> importVentasYa(
            @RequestParam("file") MultipartFile file,
            @RequestParam("eventName") String eventName
    ) {
        // Basic validation can stay in controller; heavy lifting is done in the service
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(MigrationResult.error("Empty file"));
        }
        if (eventName == null || eventName.isBlank()) {
            return ResponseEntity.badRequest().body(MigrationResult.error("Missing eventName"));
        }
        MigrationResult result = migrationService.importVentasYa(file, eventName);
        return ResponseEntity.ok(result);
    }

    @PostMapping(path = "/importarLite", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MigrationResult> importarLite(
            @RequestParam("file") MultipartFile file,
            @RequestParam("eventName") String eventName
    ) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(MigrationResult.error("Empty file"));
        }
        if (eventName == null || eventName.isBlank()) {
            return ResponseEntity.badRequest().body(MigrationResult.error("Missing eventName"));
        }
        MigrationResult result = migrationService.importarLite(file, eventName);
        return ResponseEntity.ok(result);
    }
}
