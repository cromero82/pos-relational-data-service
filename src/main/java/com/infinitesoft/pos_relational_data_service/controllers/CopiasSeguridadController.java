package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.dto.RestoreBackupResponseDto;
import com.infinitesoft.pos_relational_data_service.security.dto.AuthUserDto;
import com.infinitesoft.pos_relational_data_service.security.util.SecurityContextHelper;
import com.infinitesoft.pos_relational_data_service.services.BackupService;
import com.infinitesoft.pos_relational_data_service.services.RestoreBackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.Callable;

@RestController
@RequestMapping("/copias-seguridad")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('admin')")
public class CopiasSeguridadController {

    private final BackupService backupService;
    private final RestoreBackupService restoreBackupService;

    @GetMapping("/generar-backup")
    public ResponseEntity<?> generarBackup(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            byte[] reportBytes = backupService.generarBackupExcel(token);

            String fileName = "Backup Gestor Market " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")) + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(reportBytes);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "error", true,
                            "message", "Error al generar el backup",
                            "detalle", e.getMessage() != null ? e.getMessage() : "Error desconocido"
                    ));
        }
    }

    @GetMapping("/exportar-a-correo")
    public Callable<ResponseEntity<Map<String, String>>> exportarACorreo(@RequestHeader("Authorization") String authHeader) {
        return () -> {
            String token = authHeader.replace("Bearer ", "");
            AuthUserDto user = SecurityContextHelper.getUser();
            if (user == null || user.getCorreoElectronico() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "No se pudo obtener el correo del usuario"));
            }

            backupService.enviarBackupPorCorreo(token, user.getCorreoElectronico());

            return ResponseEntity.ok(Map.of("message", "Backup enviado correctamente al correo: " + user.getCorreoElectronico()));
        };
    }

    @PostMapping("/restaurar-backup")
    public ResponseEntity<?> restaurarBackup(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "tipo", defaultValue = "incremental") String tipo) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", true,
                        "message", "El archivo está vacío"
                ));
            }

            if (!tipo.equals("full-reescritura") && !tipo.equals("incremental")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", true,
                        "message", "El parámetro 'tipo' debe ser 'full-reescritura' o 'incremental'"
                ));
            }

            RestoreBackupResponseDto response = restoreBackupService.restaurarBackup(file, tipo);

            if (response.isExito()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", true,
                    "message", "Error al restaurar el backup",
                    "detalle", e.getMessage() != null ? e.getMessage() : "Error desconocido"
            ));
        }
    }
}
