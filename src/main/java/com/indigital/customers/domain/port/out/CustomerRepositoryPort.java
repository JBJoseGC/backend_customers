package com.indigital.customers.domain.port.out;

import com.indigital.customers.domain.model.Customer;
import com.indigital.customers.domain.model.CustomerFilter;

import java.util.List;

public interface CustomerRepositoryPort {

    Customer save(Customer customer);

    boolean existsByDni(String dni);

    boolean existsByEmail(String email);

    List<Customer> findAll();

    List<Customer> findByFilter(CustomerFilter filter);
}
