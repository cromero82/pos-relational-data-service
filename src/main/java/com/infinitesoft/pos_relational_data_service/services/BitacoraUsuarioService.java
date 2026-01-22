package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.BitacoraUsuarioDto;
import com.infinitesoft.pos_relational_data_service.dto.BitacoraUsuarioRequest;
import com.infinitesoft.pos_relational_data_service.entities.BitacoraUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BitacoraUsuarioService {
    List<BitacoraUsuario> findAll();
    Optional<BitacoraUsuario> findById(Integer id);
    BitacoraUsuario save(BitacoraUsuario bitacoraUsuario);
    BitacoraUsuario save(BitacoraUsuarioRequest request);
    void deleteById(Integer id);
    Page<BitacoraUsuarioDto> search(UUID userId, LocalDate fecha, Pageable pageable, String token);
}
