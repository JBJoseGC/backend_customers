package com.indigital.customers.domain.model;

import java.math.BigDecimal;

public record MonthlyBirthRate(int month, String monthName, long total, BigDecimal rate) {
}
