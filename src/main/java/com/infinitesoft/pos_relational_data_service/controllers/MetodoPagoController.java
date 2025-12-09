package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.MetodoPago;
import com.infinitesoft.pos_relational_data_service.services.MetodoPagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero','invitado')")
@RequestMapping("/metodos-pago")
@CrossOrigin(origins = "*")
public class MetodoPagoController {

    @Autowired
    private MetodoPagoService service;

    @GetMapping
    public List<MetodoPago> findAll() {
        return service.findAll();
    }
}
