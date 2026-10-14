package com.indigital.customers.application.service;

import com.indigital.customers.domain.exception.BusinessException;
import com.indigital.customers.domain.exception.CustomerAlreadyExistsException;
import com.indigital.customers.domain.model.Customer;
import com.indigital.customers.domain.model.CustomerFilter;
import com.indigital.customers.domain.model.CustomerIndicators;
import com.indigital.customers.domain.model.MonthYearCount;
import com.indigital.customers.domain.model.MonthlyBirthRate;
import com.indigital.customers.domain.port.in.CreateCustomerUseCase;
import com.indigital.customers.domain.port.in.FindCustomersUseCase;
import com.indigital.customers.domain.port.in.GetCustomerIndicatorsUseCase;
import com.indigital.customers.domain.port.out.CustomerRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class CustomerService implements CreateCustomerUseCase, FindCustomersUseCase, GetCustomerIndicatorsUseCase {

    private static final Locale LOCALE_ES = Locale.forLanguageTag("es-PE");

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepositoryPort customerRepositoryPort;
    private final Clock clock;

    public CustomerService(CustomerRepositoryPort customerRepositoryPort, Clock clock) {
        this.customerRepositoryPort = customerRepositoryPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Customer create(Customer customer) {
        Customer normalized = normalize(customer);

        if (normalized.birthDate().isAfter(LocalDate.now(clock))) {
            throw new BusinessException("La fecha de nacimiento no puede ser futura");
        }
        if (customerRepositoryPort.existsByDni(normalized.dni())) {
            throw new CustomerAlreadyExistsException("Ya existe un cliente con el DNI " + normalized.dni());
        }
        if (customerRepositoryPort.existsByEmail(normalized.email())) {
            throw new CustomerAlreadyExistsException("Ya existe un cliente con el email " + normalized.email());
        }

        Customer saved = customerRepositoryPort.save(normalized.withCreatedAt(LocalDateTime.now(clock)));
        log.info("Cliente creado id={} dni={}", saved.id(), saved.dni());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> find(CustomerFilter filter) {
        if (filter == null || filter.isEmpty()) {
            return customerRepositoryPort.findAll();
        }
        return customerRepositoryPort.findByFilter(new CustomerFilter(
                trimToNull(filter.dni()),
                filter.email() == null ? null : trimToNull(filter.email().toLowerCase())));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerIndicators getIndicators() {
        List<Customer> customers = customerRepositoryPort.findAll();
        long total = customers.size();

        // Agrupacion por mes/año de nacimiento, ordenada cronologicamente
        Map<YearMonth, Long> byYearMonth = customers.stream()
                .collect(Collectors.groupingBy(c -> YearMonth.from(c.birthDate()), TreeMap::new, Collectors.counting()));

        List<MonthYearCount> birthsByMonthYear = byYearMonth.entrySet().stream()
                .map(e -> toMonthYearCount(e.getKey(), e.getValue()))
                .toList();

        return new CustomerIndicators(
                total,
                birthsByMonthYear,
                filterByTotal(birthsByMonthYear, Collections::max),
                filterByTotal(birthsByMonthYear, Collections::min),
                buildBirthRateByMonth(customers, total));
    }

    // Tasa de natalidad = nacidos en el mes (Ene..Dic) / total de clientes * 100
    private List<MonthlyBirthRate> buildBirthRateByMonth(List<Customer> customers, long total) {
        Map<Integer, Long> byMonth = customers.stream()
                .collect(Collectors.groupingBy(c -> c.birthDate().getMonthValue(), Collectors.counting()));

        return IntStream.rangeClosed(1, 12)
                .mapToObj(month -> {
                    long count = byMonth.getOrDefault(month, 0L);
                    BigDecimal rate = total == 0
                            ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.valueOf(count * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
                    return new MonthlyBirthRate(month, monthName(Month.of(month)), count, rate);
                })
                .toList();
    }

    private List<MonthYearCount> filterByTotal(List<MonthYearCount> items,
                                               Function<List<Long>, Long> selector) {
        if (items.isEmpty()) {
            return List.of();
        }
        long target = selector.apply(items.stream().map(MonthYearCount::total).toList());
        return items.stream()
                .filter(i -> i.total() == target)
                .sorted(Comparator.comparingInt(MonthYearCount::year).thenComparingInt(MonthYearCount::month))
                .toList();
    }

    private MonthYearCount toMonthYearCount(YearMonth ym, long total) {
        String period = monthName(ym.getMonth()) + " " + ym.getYear();
        return new MonthYearCount(ym.getYear(), ym.getMonthValue(), period, total);
    }

    private String monthName(Month month) {
        String name = month.getDisplayName(TextStyle.FULL, LOCALE_ES);
        return name.substring(0, 1).toUpperCase(LOCALE_ES) + name.substring(1);
    }

    private Customer normalize(Customer customer) {
        return new Customer(
                customer.id(),
                customer.firstName().trim(),
                customer.lastName().trim(),
                customer.email().trim().toLowerCase(),
                customer.dni().trim(),
                customer.birthDate(),
                customer.createdAt());
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
