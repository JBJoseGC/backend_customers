package com.indigital.customers.infrastructure.adapter.out.persistence.mapper;

import com.indigital.customers.domain.model.Customer;
import com.indigital.customers.infrastructure.adapter.out.persistence.entity.CustomerEntity;
import org.springframework.stereotype.Component;

@Component
public class CustomerEntityMapper {

    public CustomerEntity toEntity(Customer customer) {
        return CustomerEntity.builder()
                .id(customer.id())
                .firstName(customer.firstName())
                .lastName(customer.lastName())
                .email(customer.email())
                .dni(customer.dni())
                .birthDate(customer.birthDate())
                .createdAt(customer.createdAt())
                .build();
    }

    public Customer toDomain(CustomerEntity entity) {
        return new Customer(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getDni(),
                entity.getBirthDate(),
                entity.getCreatedAt());
    }
}
