package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findFirstByNombreIgnoreCase(String nombre);
}
