package no.aurel.backend.addon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import no.aurel.backend.addon.dto.AddOnSelection;
import no.aurel.backend.salonservice.SalonService;
import no.aurel.backend.servicegroup.ServiceGroup;

@ExtendWith(MockitoExtension.class)
class AddOnServiceTest {

    @Mock
    private AddOnRepository repository;

    @InjectMocks
    private AddOnService addOnService;

    // Boxes: 1 = Nytt sett, 2 = Påfyll, 3 = Pedikyr
    private static final long NEW_SET = 1, REFILL = 2, PEDICURE = 3;

    private static SalonService treatment(long groupId) {
        ServiceGroup group = new ServiceGroup();
        group.setId(groupId);
        SalonService service = new SalonService();
        service.setGroup(group);
        return service;
    }

    private static AddOn addOn(long id, Set<Long> groups, int price, int minutes, int max) {
        AddOn addOn = new AddOn();
        addOn.setId(id);
        addOn.setName("Extra " + id);
        addOn.setGroupIds(groups);
        addOn.setPriceNok(price);
        addOn.setDurationMinutes(minutes);
        addOn.setMaxQuantity(max);
        return addOn;
    }

    @Test
    void priceAndDurationAreUnitTimesQuantity() {
        AddOn glitter = addOn(1, Set.of(NEW_SET, REFILL), 15, 1, 10);
        when(repository.findAllById(java.util.Set.of(1L))).thenReturn(List.of(glitter));

        List<ResolvedAddOn> result = addOnService.resolve(
                List.of(new AddOnSelection(1L, 4)), List.of(treatment(NEW_SET)));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().totalPriceNok()).isEqualTo(60);
        assertThat(result.getFirst().totalDurationMinutes()).isEqualTo(4);
    }

    @Test
    void noExtrasChosenMeansNothingToResolve() {
        assertThat(addOnService.resolve(null, List.of(treatment(NEW_SET)))).isEmpty();
        assertThat(addOnService.resolve(List.of(), List.of(treatment(NEW_SET)))).isEmpty();
    }

    @Test
    void anExtraForManicureDoesNotFitAPedicure() {
        AddOn glitter = addOn(1, Set.of(NEW_SET, REFILL), 15, 1, 10);
        when(repository.findAllById(java.util.Set.of(1L))).thenReturn(List.of(glitter));

        assertThatThrownBy(() -> addOnService.resolve(
                List.of(new AddOnSelection(1L, 2)), List.of(treatment(PEDICURE))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("cannot be added");
    }

    @Test
    void shapeChangeIsOnlyForARefill() {
        assertThat(AddOnService.fits(addOn(5, Set.of(REFILL), 100, 15, 1), List.of(treatment(REFILL))))
                .isTrue();
        assertThat(AddOnService.fits(addOn(5, Set.of(REFILL), 100, 15, 1), List.of(treatment(NEW_SET))))
                .isFalse();
    }

    @Test
    void anExtraFitsIfAnyTreatmentInTheCartMatches() {
        AddOn glitter = addOn(1, Set.of(NEW_SET, REFILL), 15, 1, 10);

        assertThat(AddOnService.fits(glitter, List.of(treatment(PEDICURE), treatment(REFILL))))
                .isTrue();
    }

    @Test
    void moreThanTheMaximumIsRejected() {
        AddOn largeNails = addOn(4, Set.of(NEW_SET, REFILL), 100, 15, 1);
        when(repository.findAllById(java.util.Set.of(4L))).thenReturn(List.of(largeNails));

        assertThatThrownBy(() -> addOnService.resolve(
                List.of(new AddOnSelection(4L, 2)), List.of(treatment(NEW_SET))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("quantity");
    }

    @Test
    void anInactiveExtraCannotBeBooked() {
        AddOn glitter = addOn(1, Set.of(NEW_SET, REFILL), 15, 1, 10);
        glitter.setActive(false);
        when(repository.findAllById(java.util.Set.of(1L))).thenReturn(List.of(glitter));

        assertThatThrownBy(() -> addOnService.resolve(
                List.of(new AddOnSelection(1L, 1)), List.of(treatment(NEW_SET))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void theSameExtraTwiceIsRejected() {
        assertThatThrownBy(() -> addOnService.resolve(
                List.of(new AddOnSelection(1L, 1), new AddOnSelection(1L, 2)),
                List.of(treatment(NEW_SET))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("twice");
    }

    @Test
    void anUnknownExtraIsRejected() {
        when(repository.findAllById(java.util.Set.of(99L))).thenReturn(List.of());

        assertThatThrownBy(() -> addOnService.resolve(
                List.of(new AddOnSelection(99L, 1)), List.of(treatment(NEW_SET))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("do not exist");
    }
}
