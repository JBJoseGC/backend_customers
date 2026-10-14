package com.indigital.customers.domain.model;

public record CustomerFilter(String dni, String email) {

    public boolean isEmpty() {
        return isBlank(dni) && isBlank(email);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
