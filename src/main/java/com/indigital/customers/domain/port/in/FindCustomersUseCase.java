package com.indigital.customers.domain.port.in;

import com.indigital.customers.domain.model.Customer;
import com.indigital.customers.domain.model.CustomerFilter;

import java.util.List;

public interface FindCustomersUseCase {

    List<Customer> find(CustomerFilter filter);
}
