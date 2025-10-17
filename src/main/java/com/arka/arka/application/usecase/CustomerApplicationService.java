package com.arka.arka.application.usecase;

import com.arka.arka.domain.model.Customer;
import com.arka.arka.domain.port.in.CustomerUseCase;
import com.arka.arka.domain.port.out.CustomerRepositoryPort;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Service for Customer Use Cases
 * This class orchestrates the business logic for Customer operations
 * Following Hexagonal Architecture - Application Layer
 */
public class CustomerApplicationService implements CustomerUseCase {
    
    private final CustomerRepositoryPort customerRepository;

    public CustomerApplicationService(CustomerRepositoryPort customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }

    @Override
    public Customer createCustomer(Customer customer) {
        validateCustomer(customer);
        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(Long id, Customer customer) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Customer not found with id: " + id);
        }
        validateCustomer(customer);
        customer.setId(id);
        return customerRepository.save(customer);
    }

    @Override
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
    }

    @Override
    public List<Customer> searchCustomersByName(String name) {
        return customerRepository.findByNombreStartingWith(name);
    }

    @Override
    public List<Customer> getAllCustomersSorted() {
        return customerRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Customer::getNombre, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    /**
     * Domain validation logic for Customer
     */
    private void validateCustomer(Customer customer) {
        if (customer.getNombre() == null || customer.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be empty");
        }
        if (!customer.isValidEmail()) {
            throw new IllegalArgumentException("Customer must have a valid email");
        }
    }
}
