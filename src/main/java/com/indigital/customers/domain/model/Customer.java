package com.indigital.customers.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Customer(
        Long id,
        String firstName,
        String lastName,
        String email,
        String dni,
        LocalDate birthDate,
        LocalDateTime createdAt) {

    public Customer withCreatedAt(LocalDateTime value) {
        return new Customer(null, firstName, lastName, email, dni, birthDate, value);
    }
}
