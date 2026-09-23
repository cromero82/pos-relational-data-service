package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.entities.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
}
