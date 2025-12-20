package com.infinitesoft.pos_relational_data_service.services;

import org.springframework.web.multipart.MultipartFile;

public interface MigrationService {
    MigrationResult importVentasYa(MultipartFile file, String eventName);
    MigrationResult importarLite(MultipartFile file, String eventName);
}
