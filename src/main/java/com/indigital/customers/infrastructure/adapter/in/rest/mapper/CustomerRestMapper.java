package com.indigital.customers.infrastructure.adapter.in.rest.mapper;

import com.indigital.customers.domain.model.Customer;
import com.indigital.customers.infrastructure.adapter.in.rest.dto.CustomerRequest;
import com.indigital.customers.infrastructure.adapter.in.rest.dto.CustomerResponse;
import org.springframework.stereotype.Component;

@Component
public class CustomerRestMapper {

    public Customer toDomain(CustomerRequest request) {
        return new Customer(
                null,
                request.firstName(),
                request.lastName(),
                request.email(),
                request.dni(),
                request.birthDate(),
                null);
    }

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.id(),
                customer.firstName(),
                customer.lastName(),
                customer.email(),
                customer.dni(),
                customer.birthDate(),
                customer.createdAt());
    }
}
