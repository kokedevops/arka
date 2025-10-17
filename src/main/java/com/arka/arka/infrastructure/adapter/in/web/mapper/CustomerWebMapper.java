package com.arka.arka.infrastructure.adapter.in.web.mapper;

import com.arka.arka.domain.model.Customer;
import com.arka.arka.infrastructure.adapter.in.web.dto.CustomerDto;
import org.springframework.stereotype.Component;

@Component
public class CustomerWebMapper {

    public CustomerDto toDto(Customer domain) {
        if (domain == null) {
            return null;
        }
        
        CustomerDto dto = new CustomerDto();
        dto.setId(domain.getId());
        dto.setNombre(domain.getNombre());
        dto.setEmail(domain.getEmail());
        dto.setTelefono(domain.getTelefono());
        dto.setPais(domain.getPais());
        dto.setCiudad(domain.getCiudad());
        
        return dto;
    }

    public Customer toDomain(CustomerDto dto) {
        if (dto == null) {
            return null;
        }
        
        Customer customer = new Customer();
        customer.setId(dto.getId());
        customer.setNombre(dto.getNombre());
        customer.setEmail(dto.getEmail());
        customer.setTelefono(dto.getTelefono());
        customer.setPais(dto.getPais());
        customer.setCiudad(dto.getCiudad());
        
        return customer;
    }
}
