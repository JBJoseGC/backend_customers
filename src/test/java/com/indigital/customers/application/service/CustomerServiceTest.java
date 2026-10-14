package com.indigital.customers.application.service;

import com.indigital.customers.domain.exception.BusinessException;
import com.indigital.customers.domain.exception.CustomerAlreadyExistsException;
import com.indigital.customers.domain.model.Customer;
import com.indigital.customers.domain.model.CustomerFilter;
import com.indigital.customers.domain.model.CustomerIndicators;
import com.indigital.customers.domain.model.MonthYearCount;
import com.indigital.customers.domain.model.MonthlyBirthRate;
import com.indigital.customers.domain.port.out.CustomerRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.groups.Tuple.tuple;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    private static final ZoneId ZONE = ZoneId.of("America/Lima");
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-14T15:00:00Z"), ZONE);

    @Mock
    private CustomerRepositoryPort customerRepositoryPort;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepositoryPort, FIXED_CLOCK);
    }

    @Nested
    @DisplayName("Creación de cliente")
    class Create {

        @Test
        @DisplayName("Crea el cliente normalizando datos y asignando la fecha de creación")
        void shouldCreateCustomer() {
            Customer input = customer(null, " Juan ", " Pérez ", " Juan.Perez@Mail.com ", "12345678", LocalDate.of(1990, 5, 20));
            when(customerRepositoryPort.existsByDni("12345678")).thenReturn(false);
            when(customerRepositoryPort.existsByEmail("juan.perez@mail.com")).thenReturn(false);
            when(customerRepositoryPort.save(any(Customer.class)))
                    .thenAnswer(inv -> withId(inv.getArgument(0), 1L));

            Customer result = customerService.create(input);

            ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
            verify(customerRepositoryPort).save(captor.capture());
            Customer saved = captor.getValue();

            assertThat(saved.firstName()).isEqualTo("Juan");
            assertThat(saved.lastName()).isEqualTo("Pérez");
            assertThat(saved.email()).isEqualTo("juan.perez@mail.com");
            assertThat(saved.createdAt()).isEqualTo(LocalDateTime.now(FIXED_CLOCK));
            assertThat(result.id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Rechaza un DNI ya registrado")
        void shouldFailWhenDniExists() {
            when(customerRepositoryPort.existsByDni("12345678")).thenReturn(true);

            assertThatThrownBy(() -> customerService.create(
                    customer(null, "Juan", "Pérez", "juan@mail.com", "12345678", LocalDate.of(1990, 5, 20))))
                    .isInstanceOf(CustomerAlreadyExistsException.class)
                    .hasMessageContaining("DNI");

            verify(customerRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Rechaza un email ya registrado")
        void shouldFailWhenEmailExists() {
            when(customerRepositoryPort.existsByDni("12345678")).thenReturn(false);
            when(customerRepositoryPort.existsByEmail("juan@mail.com")).thenReturn(true);

            assertThatThrownBy(() -> customerService.create(
                    customer(null, "Juan", "Pérez", "juan@mail.com", "12345678", LocalDate.of(1990, 5, 20))))
                    .isInstanceOf(CustomerAlreadyExistsException.class)
                    .hasMessageContaining("email");

            verify(customerRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Rechaza una fecha de nacimiento futura")
        void shouldFailWhenBirthDateIsFuture() {
            assertThatThrownBy(() -> customerService.create(
                    customer(null, "Juan", "Pérez", "juan@mail.com", "12345678", LocalDate.of(2030, 1, 1))))
                    .isInstanceOf(BusinessException.class);

            verify(customerRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Consulta de clientes")
    class Find {

        @Test
        @DisplayName("Sin filtros devuelve todos los clientes")
        void shouldReturnAllWhenNoFilter() {
            List<Customer> all = List.of(
                    customer(1L, "Ana", "Flores", "ana@mail.com", "11111111", LocalDate.of(1992, 3, 10)));
            when(customerRepositoryPort.findAll()).thenReturn(all);

            assertThat(customerService.find(new CustomerFilter(null, " "))).isEqualTo(all);
            verify(customerRepositoryPort, never()).findByFilter(any());
        }

        @Test
        @DisplayName("Con filtros normaliza dni y email antes de consultar")
        void shouldFindByFilter() {
            when(customerRepositoryPort.findByFilter(any())).thenReturn(List.of());

            customerService.find(new CustomerFilter(" 11111111 ", " Ana@Mail.com "));

            verify(customerRepositoryPort).findByFilter(new CustomerFilter("11111111", "ana@mail.com"));
        }
    }

    @Nested
    @DisplayName("Indicadores")
    class Indicators {

        @Test
        @DisplayName("Calcula nacidos por mes/año, mayor, menor y tasa de natalidad")
        void shouldCalculateIndicators() {
            when(customerRepositoryPort.findAll()).thenReturn(List.of(
                    customer(1L, "A", "A", "a@mail.com", "10000001", LocalDate.of(1990, 1, 5)),
                    customer(2L, "B", "B", "b@mail.com", "10000002", LocalDate.of(1990, 1, 20)),
                    customer(3L, "C", "C", "c@mail.com", "10000003", LocalDate.of(1990, 1, 28)),
                    customer(4L, "D", "D", "d@mail.com", "10000004", LocalDate.of(1995, 1, 15)),
                    customer(5L, "E", "E", "e@mail.com", "10000005", LocalDate.of(1992, 3, 10))));

            CustomerIndicators result = customerService.getIndicators();

            assertThat(result.totalCustomers()).isEqualTo(5);
            assertThat(result.birthsByMonthYear())
                    .extracting(MonthYearCount::period, MonthYearCount::total)
                    .containsExactly(
                            tuple("Enero 1990", 3L),
                            tuple("Marzo 1992", 1L),
                            tuple("Enero 1995", 1L));

            assertThat(result.highestBirthMonthYear()).extracting(MonthYearCount::period).containsExactly("Enero 1990");
            assertThat(result.lowestBirthMonthYear()).extracting(MonthYearCount::period)
                    .containsExactly("Marzo 1992", "Enero 1995");

            assertThat(result.birthRateByMonth()).hasSize(12);
            MonthlyBirthRate january = result.birthRateByMonth().get(0);
            assertThat(january.total()).isEqualTo(4);
            assertThat(january.rate()).isEqualByComparingTo(new BigDecimal("80.00"));
            assertThat(result.birthRateByMonth().get(2).rate()).isEqualByComparingTo(new BigDecimal("20.00"));
            assertThat(result.birthRateByMonth().get(1).rate()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Sin clientes devuelve indicadores vacíos y tasas en cero")
        void shouldReturnEmptyIndicators() {
            when(customerRepositoryPort.findAll()).thenReturn(List.of());

            CustomerIndicators result = customerService.getIndicators();

            assertThat(result.totalCustomers()).isZero();
            assertThat(result.birthsByMonthYear()).isEmpty();
            assertThat(result.highestBirthMonthYear()).isEmpty();
            assertThat(result.lowestBirthMonthYear()).isEmpty();
            assertThat(result.birthRateByMonth()).hasSize(12)
                    .allSatisfy(r -> assertThat(r.rate()).isEqualByComparingTo(BigDecimal.ZERO));
        }
    }

    private static Customer customer(Long id, String firstName, String lastName, String email, String dni,
                                     LocalDate birthDate) {
        return new Customer(id, firstName, lastName, email, dni, birthDate, null);
    }

    private static Customer withId(Customer c, Long id) {
        return new Customer(id, c.firstName(), c.lastName(), c.email(), c.dni(), c.birthDate(), c.createdAt());
    }
}
