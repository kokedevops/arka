package com.arka.arka.infrastructure.adapter.in.web;

import com.arka.arka.domain.model.Customer;
import com.arka.arka.domain.port.in.CustomerUseCase;
import com.arka.arka.infrastructure.adapter.in.web.dto.CustomerDto;
import com.arka.arka.infrastructure.adapter.in.web.mapper.CustomerWebMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
public class CustomerController {

    private final CustomerUseCase customerUseCase;
    private final CustomerWebMapper mapper;

    public CustomerController(CustomerUseCase customerUseCase, CustomerWebMapper mapper) {
        this.customerUseCase = customerUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<CustomerDto>> getAllUsers() {
        List<Customer> customers = customerUseCase.getAllCustomers();
        List<CustomerDto> customerDtos = customers.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customerDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getUserById(@PathVariable Long id) {
        try {
            Customer customer = customerUseCase.getCustomerById(id);
            return ResponseEntity.ok(mapper.toDto(customer));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<CustomerDto> createUser(@RequestBody CustomerDto dto) {
        try {
            Customer customer = mapper.toDomain(dto);
            Customer savedCustomer = customerUseCase.createCustomer(customer);
            return ResponseEntity.ok(mapper.toDto(savedCustomer));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto> updateUser(@PathVariable Long id, @RequestBody CustomerDto dto) {
        try {
            Customer customer = mapper.toDomain(dto);
            Customer updatedCustomer = customerUseCase.updateCustomer(id, customer);
            return ResponseEntity.ok(mapper.toDto(updatedCustomer));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            customerUseCase.deleteCustomer(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<CustomerDto>> searchByName(@RequestParam String nombre) {
        List<Customer> customers = customerUseCase.searchCustomersByName(nombre);
        List<CustomerDto> customerDtos = customers.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customerDtos);
    }

    @GetMapping("/ordenados")
    public ResponseEntity<List<CustomerDto>> getAllUsersSorted() {
        List<Customer> customers = customerUseCase.getAllCustomersSorted();
        List<CustomerDto> customerDtos = customers.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customerDtos);
    }
}
