package no.aurel.backend.salonservice;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.salonservice.dto.SalonServiceRequest;
import no.aurel.backend.salonservice.dto.SalonServiceResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class SalonServiceService {

    private final SalonServiceRepository repository;

    @Transactional(readOnly = true)
    public List<SalonServiceResponse> findAll() {
        return repository.findAll().stream()
                .map(SalonServiceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SalonServiceResponse findById(Long id) {
        return SalonServiceResponse.from(getOrThrow(id));
    }

    public SalonServiceResponse create(SalonServiceRequest request) {
        SalonService entity = new SalonService();
        apply(entity, request);
        return SalonServiceResponse.from(repository.save(entity));
    }

    public SalonServiceResponse update(Long id, SalonServiceRequest request) {
        SalonService entity = getOrThrow(id);
        apply(entity, request);
        return SalonServiceResponse.from(entity);
    }

    public void delete(Long id) {
        repository.delete(getOrThrow(id));
    }

    private SalonService getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Service " + id + " not found"));
    }

    private void apply(SalonService entity, SalonServiceRequest request) {
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setDurationMinutes(request.durationMinutes());
        entity.setPriceNok(request.priceNok());
        entity.setActive(request.active());
    }
}
