package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.GrupoEspejoDetalleResponse;
import com.infinitesoft.pos_relational_data_service.dto.GrupoEspejoRequest;
import com.infinitesoft.pos_relational_data_service.dto.ProductoEspejoDto;
import com.infinitesoft.pos_relational_data_service.entities.GrupoEspejo;
import com.infinitesoft.pos_relational_data_service.entities.Product;
import com.infinitesoft.pos_relational_data_service.repositories.GrupoEspejoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.ProductRepository;
import com.infinitesoft.pos_relational_data_service.services.GrupoEspejoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class GrupoEspejoServiceImpl implements GrupoEspejoService {

    private static final Logger log = LogManager.getLogger(GrupoEspejoServiceImpl.class);

    @Autowired
    private GrupoEspejoRepository grupoEspejoRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<GrupoEspejoDetalleResponse> obtenerTodosConProductos(String query) {
        log.info("GrupoEspejoServiceImpl - obtenerTodosConProductos: query={}", query);

        List<GrupoEspejo> grupos;

        if (query != null && !query.isBlank()) {
            String q = query.trim().toUpperCase();
            List<Product> coincidencias = productRepository.findConGrupoPorBarcodeONombre(q);
            List<Long> grupoIds = coincidencias.stream()
                    .map(p -> p.getGrupoEspejo().getId())
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
            if (grupoIds.isEmpty()) {
                return java.util.Collections.emptyList();
            }
            grupos = grupoEspejoRepository.findAllById(grupoIds);
        } else {
            grupos = grupoEspejoRepository.findAll();
        }

        List<GrupoEspejoDetalleResponse> resultado = new java.util.ArrayList<>();
        for (GrupoEspejo grupo : grupos) {
            List<Product> productos = productRepository.findByGrupoEspejoId(grupo.getId());
            List<ProductoEspejoDto> productosDto = productos.stream()
                    .map(p -> new ProductoEspejoDto(
                            p.getId(),
                            p.getNombre(),
                            p.getPrecio(),
                            p.getPrecioCompra(),
                            p.getPrecioUnidad(),
                            p.getPorcentajeGanancia(),
                            p.getFechaUltimaActualizacionPrecio()
                    ))
                    .collect(java.util.stream.Collectors.toList());

            resultado.add(new GrupoEspejoDetalleResponse(
                    grupo.getId(),
                    grupo.getNombre(),
                    grupo.getFechaCreacion(),
                    grupo.getFechaActualizacion(),
                    grupo.getProductoReferenciaId(),
                    productosDto
            ));
        }

        return resultado;
    }

    @Override
    @Transactional
    public GrupoEspejo crearGrupoEspejo(GrupoEspejoRequest request) {
        log.info("GrupoEspejoServiceImpl - crearGrupoEspejo: nombre={}", request.getNombre());

        GrupoEspejo grupo = GrupoEspejo.builder()
                .nombre(request.getNombre().toUpperCase())
                .build();
        grupo = grupoEspejoRepository.save(grupo);

        List<Long> productoIds = request.getProductoIds();
        Long ultimoProductoId = null;

        if (productoIds != null && !productoIds.isEmpty()) {
            for (Long productoId : productoIds) {
                if (!productRepository.existsById(productoId)) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado: " + productoId);
                }
                Optional<Long> grupoExistente = grupoEspejoRepository.findGrupoEspejoIdByProductoId(productoId);
                if (grupoExistente.isPresent()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "El producto " + productoId + " ya pertenece al grupo espejo " + grupoExistente.get());
                }
            }
            for (Long productoId : productoIds) {
                grupoEspejoRepository.insertProductoEnGrupo(grupo.getId(), productoId);
                ultimoProductoId = productoId;
            }
        }

        if (ultimoProductoId != null) {
            grupo.setProductoReferenciaId(ultimoProductoId);
            grupo = grupoEspejoRepository.save(grupo);
        }

        return grupo;
    }

    @Override
    @Transactional
    public GrupoEspejo agregarProducto(Long grupoEspejoId, Long productoId) {
        log.info("GrupoEspejoServiceImpl - agregarProducto: grupoEspejoId={}, productoId={}", grupoEspejoId, productoId);

        GrupoEspejo grupo = grupoEspejoRepository.findById(grupoEspejoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "GrupoEspejo no encontrado: " + grupoEspejoId));

        if (!productRepository.existsById(productoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado: " + productoId);
        }

        Optional<Long> grupoActual = grupoEspejoRepository.findGrupoEspejoIdByProductoId(productoId);
        if (grupoActual.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El producto " + productoId + " ya pertenece al grupo espejo " + grupoActual.get());
        }

        grupoEspejoRepository.insertProductoEnGrupo(grupoEspejoId, productoId);

        grupo.setProductoReferenciaId(productoId);
        return grupoEspejoRepository.save(grupo);
    }

    @Override
    @Transactional
    public void quitarProducto(Long grupoEspejoId, Long productoId) {
        log.info("GrupoEspejoServiceImpl - quitarProducto: grupoEspejoId={}, productoId={}", grupoEspejoId, productoId);

        GrupoEspejo grupo = grupoEspejoRepository.findById(grupoEspejoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "GrupoEspejo no encontrado: " + grupoEspejoId));

        int deleted = grupoEspejoRepository.deleteProductoFromGrupo(grupoEspejoId, productoId);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "El producto " + productoId + " no pertenece al grupo espejo " + grupoEspejoId);
        }

        Optional<Long> nuevoReferencia = grupoEspejoRepository.findUltimoProductoIdEnGrupo(grupoEspejoId);
        grupo.setProductoReferenciaId(nuevoReferencia.orElse(null));
        grupoEspejoRepository.save(grupo);
    }
}
