package no.aurel.backend.servicegroup;

import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.image.ImageFormat;
import no.aurel.backend.salonservice.SalonServiceRepository;
import no.aurel.backend.servicegroup.dto.ServiceGroupRequest;
import no.aurel.backend.servicegroup.dto.ServiceGroupResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class ServiceGroupService {

    private final ServiceGroupRepository repository;
    private final ServiceGroupImageRepository imageRepository;
    private final SalonServiceRepository salonServiceRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<ServiceGroupResponse> findAll() {
        return repository.findAll(Sort.by("sortOrder", "id")).stream()
                .map(ServiceGroupResponse::from)
                .toList();
    }

    public ServiceGroupResponse create(ServiceGroupRequest request) {
        ServiceGroup group = new ServiceGroup();
        apply(group, request);
        // A new box goes last
        group.setSortOrder(repository.findAll().stream().mapToInt(ServiceGroup::getSortOrder).max().orElse(0) + 1);
        return ServiceGroupResponse.from(repository.save(group));
    }

    public ServiceGroupResponse update(Long id, ServiceGroupRequest request) {
        ServiceGroup group = getOrThrow(id);
        apply(group, request);
        return ServiceGroupResponse.from(group);
    }

    // The list must contain every box exactly once, so a stale browser can never silently drop or duplicate one
    public List<ServiceGroupResponse> reorder(List<Long> ids) {
        List<ServiceGroup> all = repository.findAll();
        if (ids.size() != all.size()
                || !new HashSet<>(ids).equals(all.stream().map(ServiceGroup::getId).collect(Collectors.toSet()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The list must contain every box exactly once");
        }
        for (ServiceGroup group : all) {
            group.setSortOrder(ids.indexOf(group.getId()));
        }
        return findAll();
    }

    // Only an empty box can go: treatments are never deleted (old appointments point to them), only hidden
    public void delete(Long id) {
        ServiceGroup group = getOrThrow(id);
        if (salonServiceRepository.existsByGroupId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Boksen har behandlinger. Skjul behandlingene i stedet for å slette boksen.");
        }
        repository.delete(group);
    }

    @Transactional(readOnly = true)
    public Optional<ServiceGroupImage> findImage(Long id) {
        return imageRepository.findById(id);
    }

    public ServiceGroupResponse saveImage(Long id, byte[] data) {
        ServiceGroup group = getOrThrow(id);
        String contentType = ImageFormat.requireValid(data);

        ServiceGroupImage image = imageRepository.findById(id).orElseGet(() -> {
            ServiceGroupImage created = new ServiceGroupImage();
            created.setGroupId(id);
            return created;
        });
        image.setContentType(contentType);
        image.setData(data);
        imageRepository.save(image);

        group.setImageVersion(clock.millis());
        return ServiceGroupResponse.from(group);
    }

    // Back to the photo that ships with the website, or no photo if the box has none
    public ServiceGroupResponse deleteImage(Long id) {
        ServiceGroup group = getOrThrow(id);
        imageRepository.findById(id).ifPresent(imageRepository::delete);
        group.setImageVersion(null);
        return ServiceGroupResponse.from(group);
    }

    private void apply(ServiceGroup group, ServiceGroupRequest request) {
        group.setTitle(request.title().trim());
        group.setEyebrow(request.eyebrow() == null || request.eyebrow().isBlank() ? null : request.eyebrow().trim());
        group.setImageFocus(request.imageFocus());
    }

    private ServiceGroup getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Box " + id + " not found"));
    }
}
