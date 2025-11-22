package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.dto.HistorialReciboDetalleDto;
import com.infinitesoft.pos_relational_data_service.services.HistorialReciboDetalleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/historial-recibo-detalles")
@CrossOrigin(origins = "*")
public class HistorialReciboDetalleController {

    @Autowired
    private HistorialReciboDetalleService service;

    @GetMapping
    public List<HistorialReciboDetalleDto> findByReciboId(@RequestParam("reciboId") Long reciboId) {
        return service.findByReciboId(reciboId);
    }
}
