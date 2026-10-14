package com.indigital.customers.infrastructure.adapter.in.rest;

import com.indigital.customers.domain.model.CustomerFilter;
import com.indigital.customers.domain.model.CustomerIndicators;
import com.indigital.customers.domain.port.in.CreateCustomerUseCase;
import com.indigital.customers.domain.port.in.FindCustomersUseCase;
import com.indigital.customers.domain.port.in.GetCustomerIndicatorsUseCase;
import com.indigital.customers.infrastructure.adapter.in.rest.dto.CustomerRequest;
import com.indigital.customers.infrastructure.adapter.in.rest.dto.CustomerResponse;
import com.indigital.customers.infrastructure.adapter.in.rest.mapper.CustomerRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers")
public class CustomerController {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final FindCustomersUseCase findCustomersUseCase;
    private final GetCustomerIndicatorsUseCase getCustomerIndicatorsUseCase;
    private final CustomerRestMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear cliente")
    public CustomerResponse create(@Valid @RequestBody CustomerRequest request) {
        return mapper.toResponse(createCustomerUseCase.create(mapper.toDomain(request)));
    }

    @GetMapping
    @Operation(summary = "Consultar clientes por dni, email o sin filtros")
    public List<CustomerResponse> find(@RequestParam(required = false) String dni,
                                       @RequestParam(required = false) String email) {
        return findCustomersUseCase.find(new CustomerFilter(dni, email)).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/indicators")
    @Operation(summary = "Indicadores de nacimiento de clientes")
    public CustomerIndicators indicators() {
        return getCustomerIndicatorsUseCase.getIndicators();
    }
}
