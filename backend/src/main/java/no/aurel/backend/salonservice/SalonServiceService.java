package no.aurel.backend.salonservice;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.salonservice.dto.SalonServiceRequest;
import no.aurel.backend.salonservice.dto.SalonServiceResponse;
import no.aurel.backend.servicegroup.ServiceGroupRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class SalonServiceService {

    private final SalonServiceRepository repository;
    private final ServiceGroupRepository groupRepository;

    @Transactional(readOnly = true)
    public List<SalonServiceResponse> findAll() {
        // The admin's order; id as tie-breaker keeps the result stable
        return repository.findAll(Sort.by("sortOrder", "id")).stream()
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
        // A new treatment goes last
        entity.setSortOrder(repository.findAll().stream().mapToInt(SalonService::getSortOrder).max().orElse(0) + 1);
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
        entity.setCategory(request.category());
        entity.setSubcategory(request.subcategory());
        entity.setDurationMinutes(request.durationMinutes());
        entity.setPriceNok(request.priceNok());
        entity.setActive(request.active());
        entity.setNote(request.note());
        entity.setPriceOnRequest(request.priceOnRequest());
        entity.setGroup(groupRepository.findById(request.groupId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "The box does not exist")));
    }
}
