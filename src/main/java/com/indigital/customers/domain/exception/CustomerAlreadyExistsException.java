package com.indigital.customers.domain.exception;

public class CustomerAlreadyExistsException extends BusinessException {

    public CustomerAlreadyExistsException(String message) {
        super(message);
    }
}
