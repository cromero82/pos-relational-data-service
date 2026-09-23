package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.OrigenFondos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrigenFondosRepository extends JpaRepository<OrigenFondos, Integer> {

    @Query("SELECT c FROM OrigenFondos c LEFT JOIN FETCH c.tipoOrigenFondos WHERE c.activo = true ORDER BY c.orden ASC, c.id ASC")
    List<OrigenFondos> findByActivoTrueOrderByOrdenAscIdAsc();

    @Query("SELECT c FROM OrigenFondos c LEFT JOIN FETCH c.tipoOrigenFondos WHERE c.visibleEnEgreso = true AND c.activo = true ORDER BY c.orden ASC, c.id ASC")
    List<OrigenFondos> findByVisibleEnEgresoTrueAndActivoTrueOrderByOrdenAscIdAsc();

    /**
     * Cuenta raíz (sin padre) vinculada al medio de pago.
     * Los hijos pueden heredar el mismo metodoPagoId solo como referencia visual;
     * ventas/cierre deben impactar siempre la raíz.
     */
    @Query("SELECT c FROM OrigenFondos c WHERE c.metodoPagoId = :metodoPagoId "
            + "AND c.parentOrigenFondosId IS NULL AND c.activo = true "
            + "ORDER BY c.id ASC")
    List<OrigenFondos> findRaicesActivasByMetodoPagoId(
            @org.springframework.data.repository.query.Param("metodoPagoId") Long metodoPagoId);

    /** Preferir raíz; si no hay, cualquier activo (compatibilidad datos viejos). */
    default Optional<OrigenFondos> findByMetodoPagoId(Long metodoPagoId) {
        if (metodoPagoId == null) {
            return Optional.empty();
        }
        List<OrigenFondos> raices = findRaicesActivasByMetodoPagoId(metodoPagoId);
        if (!raices.isEmpty()) {
            return Optional.of(raices.get(0));
        }
        return findFirstByMetodoPagoIdAndActivoTrueOrderByIdAsc(metodoPagoId);
    }

    Optional<OrigenFondos> findFirstByMetodoPagoIdAndActivoTrueOrderByIdAsc(Long metodoPagoId);

    @Query("SELECT COUNT(c) > 0 FROM OrigenFondos c "
            + "WHERE c.parentOrigenFondosId = :parentId "
            + "AND c.activo = true "
            + "AND LOWER(TRIM(c.nombre)) = LOWER(TRIM(:nombre))")
    boolean existsHermanoActivoConNombre(
            @org.springframework.data.repository.query.Param("parentId") Integer parentId,
            @org.springframework.data.repository.query.Param("nombre") String nombre);

    @Query("SELECT COUNT(c) FROM OrigenFondos c "
            + "WHERE c.parentOrigenFondosId = :parentId AND c.activo = true")
    long countHijosActivos(@org.springframework.data.repository.query.Param("parentId") Integer parentId);

    @Query("SELECT COALESCE(MAX(c.orden), 0) FROM OrigenFondos c WHERE c.parentOrigenFondosId = :parentId")
    Integer maxOrdenHijos(@org.springframework.data.repository.query.Param("parentId") Integer parentId);
}
