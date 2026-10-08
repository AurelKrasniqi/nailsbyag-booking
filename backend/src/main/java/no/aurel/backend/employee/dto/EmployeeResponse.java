package no.aurel.backend.employee.dto;

import no.aurel.backend.employee.Employee;

public record EmployeeResponse(Long id, String name, String specialties) {

    public static EmployeeResponse from(Employee entity) {
        return new EmployeeResponse(entity.getId(), entity.getName(), entity.getSpecialties());
    }
}
