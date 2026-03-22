package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.Egreso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EgresoRepository extends JpaRepository<Egreso, Long> {
    List<Egreso> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);
    List<Egreso> findByProveedorId(Long proveedorId);

    @Query("SELECT e FROM Egreso e WHERE e.proveedor.tipoEgreso.id = :tipoEgresoId")
    List<Egreso> findByTipoEgresoId(@Param("tipoEgresoId") Long tipoEgresoId);

    @Query("SELECT e FROM Egreso e LEFT JOIN e.proveedor p WHERE " +
           "(:descripcion IS NULL OR LOWER(e.descripcion) LIKE LOWER(CONCAT('%', :descripcion, '%'))) AND " +
           "(:tipoEgresoId IS NULL OR p.tipoEgreso.id = :tipoEgresoId) AND " +
           "(:proveedorId IS NULL OR p.id = :proveedorId)")
    Page<Egreso> search(@Param("descripcion") String descripcion,
                        @Param("tipoEgresoId") Long tipoEgresoId,
                        @Param("proveedorId") Long proveedorId,
                        Pageable pageable);

    @Query("SELECT e FROM Egreso e LEFT JOIN e.proveedor p LEFT JOIN p.tipoEgreso t WHERE " +
           "(:texto IS NULL OR :texto = '' OR " +
           "LOWER(e.descripcion) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(t.nombre) LIKE LOWER(CONCAT('%', :texto, '%')))")
    Page<Egreso> searchDescripciones(@Param("texto") String texto, Pageable pageable);
}
