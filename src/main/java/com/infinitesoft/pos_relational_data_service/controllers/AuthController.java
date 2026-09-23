package com.infinitesoft.pos_relational_data_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth-deleted")
public class AuthController {
    // This controller has been deprecated and its logic moved to SesionController
    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("Deprecated");
    }
}
