package com.indigital.customers.domain.model;

import java.util.List;

public record CustomerIndicators(
        long totalCustomers,
        List<MonthYearCount> birthsByMonthYear,
        List<MonthYearCount> highestBirthMonthYear,
        List<MonthYearCount> lowestBirthMonthYear,
        List<MonthlyBirthRate> birthRateByMonth) {
}
