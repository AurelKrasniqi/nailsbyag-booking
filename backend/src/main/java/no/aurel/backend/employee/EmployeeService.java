package no.aurel.backend.employee;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.employee.dto.EmployeeRequest;
import no.aurel.backend.employee.dto.EmployeeResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository repository;

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll() {
        return repository.findAll().stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        return EmployeeResponse.from(getOrThrow(id));
    }

    public EmployeeResponse create(EmployeeRequest request) {
        Employee entity = new Employee();
        apply(entity, request);
        return EmployeeResponse.from(repository.save(entity));
    }

    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee entity = getOrThrow(id);
        apply(entity, request);
        return EmployeeResponse.from(entity);
    }

    public void delete(Long id) {
        repository.delete(getOrThrow(id));
    }

    private Employee getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee " + id + " not found"));
    }

    private void apply(Employee entity, EmployeeRequest request) {
        entity.setName(request.name());
        entity.setSpecialties(request.specialties());
    }
}
