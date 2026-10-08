package no.aurel.backend.customer.dto;

import no.aurel.backend.customer.Customer;

public record CustomerResponse(Long id, String name, String phone, String email) {

    public static CustomerResponse from(Customer entity) {
        return new CustomerResponse(entity.getId(), entity.getName(), entity.getPhone(), entity.getEmail());
    }
}
