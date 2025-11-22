package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.entities.EstadoRecibo;
import com.infinitesoft.pos_relational_data_service.services.EstadoReciboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/estado-recibos")
@CrossOrigin(origins = "*")
public class EstadoReciboController {

    @Autowired
    private EstadoReciboService service;

    @GetMapping
    public List<EstadoRecibo> findAll() {
        return service.findAll();
    }
}
