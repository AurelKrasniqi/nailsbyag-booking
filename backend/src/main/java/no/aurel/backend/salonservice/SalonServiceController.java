package no.aurel.backend.salonservice;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.aurel.backend.salonservice.dto.SalonServiceRequest;
import no.aurel.backend.salonservice.dto.SalonServiceResponse;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class SalonServiceController {

    private final SalonServiceService service;

    @GetMapping
    public List<SalonServiceResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public SalonServiceResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<SalonServiceResponse> create(@Valid @RequestBody SalonServiceRequest request) {
        SalonServiceResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/services/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public SalonServiceResponse update(@PathVariable Long id, @Valid @RequestBody SalonServiceRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
