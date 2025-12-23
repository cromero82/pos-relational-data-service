package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.BitacoraUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BitacoraUsuarioRepository extends JpaRepository<BitacoraUsuario, Integer>, JpaSpecificationExecutor<BitacoraUsuario> {
}
