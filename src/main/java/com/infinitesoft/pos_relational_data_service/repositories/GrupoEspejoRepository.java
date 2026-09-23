package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.GrupoEspejo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface GrupoEspejoRepository extends JpaRepository<GrupoEspejo, Long> {

    @Query(value = "SELECT grupo_espejo_id FROM producto_espejo WHERE producto_id = :productoId LIMIT 1", nativeQuery = true)
    Optional<Long> findGrupoEspejoIdByProductoId(@Param("productoId") Long productoId);

    @Query(value = "SELECT COUNT(*) FROM producto_espejo WHERE grupo_espejo_id = :grupoEspejoId AND producto_id = :productoId", nativeQuery = true)
    int existsProductoEnGrupo(@Param("grupoEspejoId") Long grupoEspejoId, @Param("productoId") Long productoId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO producto_espejo (grupo_espejo_id, producto_id, fecha_creacion) VALUES (:grupoEspejoId, :productoId, NOW())", nativeQuery = true)
    void insertProductoEnGrupo(@Param("grupoEspejoId") Long grupoEspejoId, @Param("productoId") Long productoId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM producto_espejo WHERE grupo_espejo_id = :grupoEspejoId AND producto_id = :productoId", nativeQuery = true)
    int deleteProductoFromGrupo(@Param("grupoEspejoId") Long grupoEspejoId, @Param("productoId") Long productoId);

    @Query(value = "SELECT producto_id FROM producto_espejo WHERE grupo_espejo_id = :grupoEspejoId ORDER BY fecha_creacion DESC LIMIT 1", nativeQuery = true)
    Optional<Long> findUltimoProductoIdEnGrupo(@Param("grupoEspejoId") Long grupoEspejoId);
}
