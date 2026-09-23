package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.ProductoPresentacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoPresentacionRepository extends JpaRepository<ProductoPresentacion, Long> {

    List<ProductoPresentacion> findByProductoIdAndActivoTrueOrderByEsDefaultVentaDescIdAsc(Long productoId);

    List<ProductoPresentacion> findByProductoIdOrderByEsDefaultVentaDescIdAsc(Long productoId);

    Optional<ProductoPresentacion> findByProductoIdAndCodigoAndActivoTrue(Long productoId, String codigo);

    Optional<ProductoPresentacion> findByProductoIdAndEsDefaultVentaTrueAndActivoTrue(Long productoId);

    Optional<ProductoPresentacion> findByCodigoBarrasAltAndActivoTrue(String codigoBarrasAlt);

    boolean existsByProductoIdAndCodigo(Long productoId, String codigo);
}
