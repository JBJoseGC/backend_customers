package com.indigital.customers.infrastructure.adapter.out.persistence.repository;

import com.indigital.customers.infrastructure.adapter.out.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, Long> {

    boolean existsByDni(String dni);

    boolean existsByEmailIgnoreCase(String email);

    List<CustomerEntity> findAllByOrderByIdDesc();

    @Query("""
            SELECT c FROM CustomerEntity c
             WHERE (:dni IS NULL OR c.dni = :dni)
               AND (:email IS NULL OR LOWER(c.email) = :email)
             ORDER BY c.id DESC
            """)
    List<CustomerEntity> findByFilter(@Param("dni") String dni, @Param("email") String email);
}
