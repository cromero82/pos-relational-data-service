package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.ConsecutivoDocumento;
import com.infinitesoft.pos_relational_data_service.entities.enums.TipoConsecutivoDocumento;
import com.infinitesoft.pos_relational_data_service.repositories.ConsecutivoDocumentoRepository;
import com.infinitesoft.pos_relational_data_service.services.ConsecutivoDocumentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

@Service
public class ConsecutivoDocumentoServiceImpl implements ConsecutivoDocumentoService {

    @Autowired
    private ConsecutivoDocumentoRepository repository;

    @Override
    @Transactional
    public String nextConsecutivo(TipoConsecutivoDocumento tipo) {
        int anio = Year.now().getValue();
        ConsecutivoDocumento row = repository.findByTipoAndAnio(tipo.name(), anio)
                .orElseGet(() -> repository.save(ConsecutivoDocumento.builder()
                        .tipo(tipo.name())
                        .anio(anio)
                        .ultimoNumero(0L)
                        .prefijo(tipo.getPrefijo())
                        .build()));

        long next = row.getUltimoNumero() + 1;
        row.setUltimoNumero(next);
        repository.save(row);

        return row.getPrefijo() + "-" + String.format("%06d", next);
    }
}
