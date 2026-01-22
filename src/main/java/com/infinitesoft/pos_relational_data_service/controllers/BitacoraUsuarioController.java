package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.dto.BitacoraUsuarioDto;
import com.infinitesoft.pos_relational_data_service.dto.BitacoraUsuarioRequest;
import com.infinitesoft.pos_relational_data_service.entities.BitacoraUsuario;
import com.infinitesoft.pos_relational_data_service.services.BitacoraUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bitacora-usuario")
public class BitacoraUsuarioController {

    private final BitacoraUsuarioService bitacoraUsuarioService;

    @Autowired
    public BitacoraUsuarioController(BitacoraUsuarioService bitacoraUsuarioService) {
        this.bitacoraUsuarioService = bitacoraUsuarioService;
    }

    @GetMapping
    public ResponseEntity<List<BitacoraUsuario>> getAll() {
        return ResponseEntity.ok(bitacoraUsuarioService.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<BitacoraUsuarioDto>> search(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestHeader(value = "Authorization", required = false) String token,
            @PageableDefault(sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable) {
        
        // Extraer el token puro si viene con "Bearer "
        String jwtToken = token;
        if (token != null && token.startsWith("Bearer ")) {
            jwtToken = token.substring(7);
        }
        
        return ResponseEntity.ok(bitacoraUsuarioService.search(userId, fecha, pageable, jwtToken));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BitacoraUsuario> getById(@PathVariable Integer id) {
        return bitacoraUsuarioService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BitacoraUsuario> create(@RequestBody BitacoraUsuarioRequest bitacoraUsuarioRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bitacoraUsuarioService.save(bitacoraUsuarioRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BitacoraUsuario> update(@PathVariable Integer id, @RequestBody BitacoraUsuario bitacoraUsuario) {
        return bitacoraUsuarioService.findById(id)
                .map(existingBitacora -> {
                    existingBitacora.setUserId(bitacoraUsuario.getUserId());
                    existingBitacora.setEventoId(bitacoraUsuario.getEventoId());
                    existingBitacora.setValorAntes(bitacoraUsuario.getValorAntes());
                    existingBitacora.setValorDespues(bitacoraUsuario.getValorDespues());
                    return ResponseEntity.ok(bitacoraUsuarioService.save(existingBitacora));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (bitacoraUsuarioService.findById(id).isPresent()) {
            bitacoraUsuarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
