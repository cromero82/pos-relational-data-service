package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.FuncionalidadRol;
import com.infinitesoft.pos_relational_data_service.entities.FuncionalidadRolId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface FuncionalidadRolRepository extends JpaRepository<FuncionalidadRol, FuncionalidadRolId> {

    @Query("SELECT fr FROM FuncionalidadRol fr JOIN FETCH fr.funcionalidad f " +
           "WHERE fr.rolSigla IN :siglas AND fr.puedeLeer = true AND f.activo = true")
    List<FuncionalidadRol> findPermisosByRolSiglas(@Param("siglas") Collection<String> siglas);
}
