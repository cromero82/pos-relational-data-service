package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.security.dto.AuthUserDto;
import com.infinitesoft.pos_relational_data_service.security.util.SecurityContextHelper;
import com.infinitesoft.pos_relational_data_service.services.BackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/generar-backup")
    public Callable<ResponseEntity<byte[]>> generarBackup(@RequestHeader("Authorization") String authHeader) {
        return () -> {
            String token = authHeader.replace("Bearer ", "");
            byte[] reportBytes = backupService.generarBackupExcel(token);

            String fileName = "Backup Gestor Market " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")) + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(reportBytes);
        };
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
}
