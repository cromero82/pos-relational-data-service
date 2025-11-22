package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.dto.EdicionReciboDetalleDto;
import com.infinitesoft.pos_relational_data_service.services.EdicionReciboDetalleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/edicion-recibo-detalles")
@CrossOrigin(origins = "*")
public class EdicionReciboDetalleController {

    @Autowired
    private EdicionReciboDetalleService service;

    @GetMapping
    public List<EdicionReciboDetalleDto> findByReciboId(
            @RequestParam(required = false) Long reciboId,
            @RequestParam(required = false) Long historialReciboId) {
        return service.findByReciboIdOrHistorialReciboId(reciboId, historialReciboId);
    }
}
