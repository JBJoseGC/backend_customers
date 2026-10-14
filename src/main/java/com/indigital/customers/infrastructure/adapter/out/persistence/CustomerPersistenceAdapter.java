package com.indigital.customers.infrastructure.adapter.out.persistence;

import com.indigital.customers.domain.model.Customer;
import com.indigital.customers.domain.model.CustomerFilter;
import com.indigital.customers.domain.port.out.CustomerRepositoryPort;
import com.indigital.customers.infrastructure.adapter.out.persistence.mapper.CustomerEntityMapper;
import com.indigital.customers.infrastructure.adapter.out.persistence.repository.CustomerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomerPersistenceAdapter implements CustomerRepositoryPort {

    private final CustomerJpaRepository customerJpaRepository;
    private final CustomerEntityMapper mapper;

    @Override
    public Customer save(Customer customer) {
        return mapper.toDomain(customerJpaRepository.save(mapper.toEntity(customer)));
    }

    @Override
    public boolean existsByDni(String dni) {
        return customerJpaRepository.existsByDni(dni);
    }

    @Override
    public boolean existsByEmail(String email) {
        return customerJpaRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public List<Customer> findAll() {
        return customerJpaRepository.findAllByOrderByIdDesc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Customer> findByFilter(CustomerFilter filter) {
        return customerJpaRepository.findByFilter(filter.dni(), filter.email()).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
