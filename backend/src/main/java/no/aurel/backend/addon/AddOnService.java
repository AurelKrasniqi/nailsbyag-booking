package no.aurel.backend.addon;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.addon.dto.AddOnResponse;
import no.aurel.backend.addon.dto.AddOnSelection;
import no.aurel.backend.addon.dto.AddOnUpdateRequest;
import no.aurel.backend.salonservice.SalonService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddOnService {

    private final AddOnRepository repository;

    public List<AddOnResponse> findActive() {
        return repository.findByActiveTrueOrderBySortOrder().stream()
                .map(AddOnResponse::from)
                .toList();
    }

    // For the admin page: also the extras that are switched off
    public List<AddOnResponse> findAll() {
        return repository.findAll(Sort.by("sortOrder")).stream()
                .map(AddOnResponse::from)
                .toList();
    }

    @Transactional
    public AddOnResponse update(Long id, AddOnUpdateRequest request) {
        AddOn addOn = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Extra " + id + " not found"));
        addOn.setPriceNok(request.priceNok());
        addOn.setDurationMinutes(request.durationMinutes());
        addOn.setActive(request.active());
        return AddOnResponse.from(addOn);
    }

    /**
     * Turns the customer's choices into checked add-ons. The browser is never trusted: every id, quantity and
     * "does this extra fit the chosen treatment?" is verified here, because price and duration come from this.
     */
    public List<ResolvedAddOn> resolve(List<AddOnSelection> selections, List<SalonService> services) {
        if (selections == null || selections.isEmpty()) {
            return List.of();
        }

        Set<Long> ids = new HashSet<>();
        for (AddOnSelection selection : selections) {
            if (!ids.add(selection.addOnId())) {
                throw badRequest("The same extra was chosen twice");
            }
        }

        List<AddOn> found = repository.findAllById(ids);
        if (found.size() != ids.size()) {
            throw badRequest("One or more extras do not exist");
        }

        return selections.stream()
                .map(selection -> {
                    AddOn addOn = found.stream().filter(a -> a.getId().equals(selection.addOnId())).findFirst().orElseThrow();
                    check(addOn, selection.quantity(), services);
                    return new ResolvedAddOn(addOn, selection.quantity());
                })
                .sorted(Comparator.comparing(resolved -> resolved.addOn().getSortOrder()))
                .toList();
    }

    private void check(AddOn addOn, int quantity, List<SalonService> services) {
        if (!addOn.isActive()) {
            throw badRequest(addOn.getName() + " is not available");
        }
        if (quantity < 1 || quantity > addOn.getMaxQuantity()) {
            throw badRequest(addOn.getName() + ": quantity must be between 1 and " + addOn.getMaxQuantity());
        }
        if (!fits(addOn, services)) {
            throw badRequest(addOn.getName() + " cannot be added to the chosen treatments");
        }
    }

    // An extra fits when at least one chosen treatment is in its category (and subcategory, if it has one)
    static boolean fits(AddOn addOn, List<SalonService> services) {
        return services.stream().anyMatch(service ->
                addOn.getCategory().equals(service.getCategory())
                        && (addOn.getSubcategory() == null || addOn.getSubcategory().equals(service.getSubcategory())));
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
