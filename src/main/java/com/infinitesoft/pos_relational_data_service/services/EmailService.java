package com.infinitesoft.pos_relational_data_service.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Service
@Slf4j
public class EmailService {

    private final WebClient webClient;
    private final String sendFilePath;
    private final Duration timeout;

    public EmailService(
            WebClient.Builder webClientBuilder,
            @Value("${mail.service.host:http://localhost:8082}") String serviceHost,
            @Value("${mail.service.base-path:/api/v1/mail}") String basePath,
            @Value("${mail.service.path.send-file:/send-file}") String sendFilePath,
            @Value("${mail.service.timeout-ms:10000}") long timeoutMs
    ) {
        String baseUrl = serviceHost + basePath;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.sendFilePath = sendFilePath;
        this.timeout = Duration.ofMillis(timeoutMs);
    }

    public void enviarCorreoConAdjunto(String to, String subject, String messageHtml, byte[] attachment, String fileName) {
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("to", to);
            builder.part("subject", subject);
            builder.part("message", messageHtml);
            
            // Adjuntar el archivo como un recurso de bytes
            builder.part("file", new ByteArrayResource(attachment))
                    .filename(fileName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM);

            MultiValueMap<String, HttpEntity<?>> multipartBody = builder.build();

            log.info("[EmailService] Enviando correo a {} vía microservicio externo", to);

            String response = webClient.post()
                    .uri(sendFilePath)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(multipartBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(timeout);

            log.info("[EmailService] Respuesta del microservicio de correo: {}", response);
        } catch (Exception e) {
            log.error("[EmailService] Error enviando correo vía microservicio: {}", e.toString());
            // No relanzamos excepción para no romper el proceso de backup si falla el correo
            // pero en BackupService se manejaba con throws Exception, así que mantenemos consistencia si es necesario
            throw new RuntimeException("Error enviando correo a través del microservicio", e);
        }
    }
}
