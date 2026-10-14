package com.indigital.customers.domain.port.in;

import com.indigital.customers.domain.model.Customer;

public interface CreateCustomerUseCase {

    Customer create(Customer customer);
}
