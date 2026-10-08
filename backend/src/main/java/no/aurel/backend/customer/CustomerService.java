package no.aurel.backend.customer;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.customer.dto.CustomerRequest;
import no.aurel.backend.customer.dto.CustomerResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository repository;

    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return repository.findAll().stream()
                .map(CustomerResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        return CustomerResponse.from(getOrThrow(id));
    }

    public CustomerResponse create(CustomerRequest request) {
        Customer entity = new Customer();
        apply(entity, request);
        return CustomerResponse.from(repository.save(entity));
    }

    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer entity = getOrThrow(id);
        apply(entity, request);
        return CustomerResponse.from(entity);
    }

    public void delete(Long id) {
        repository.delete(getOrThrow(id));
    }

    private Customer getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer " + id + " not found"));
    }

    private void apply(Customer entity, CustomerRequest request) {
        entity.setName(request.name());
        entity.setPhone(request.phone());
        entity.setEmail(request.email());
    }
}
