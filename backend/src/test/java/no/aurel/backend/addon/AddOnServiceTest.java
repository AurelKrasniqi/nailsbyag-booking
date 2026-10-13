package no.aurel.backend.addon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import no.aurel.backend.addon.dto.AddOnSelection;
import no.aurel.backend.salonservice.SalonService;

@ExtendWith(MockitoExtension.class)
class AddOnServiceTest {

    @Mock
    private AddOnRepository repository;

    @InjectMocks
    private AddOnService addOnService;

    private static SalonService treatment(String category, String subcategory) {
        SalonService service = new SalonService();
        service.setCategory(category);
        service.setSubcategory(subcategory);
        return service;
    }

    private static AddOn addOn(long id, String category, String subcategory, int price, int minutes, int max) {
        AddOn addOn = new AddOn();
        addOn.setId(id);
        addOn.setName("Extra " + id);
        addOn.setCategory(category);
        addOn.setSubcategory(subcategory);
        addOn.setPriceNok(price);
        addOn.setDurationMinutes(minutes);
        addOn.setMaxQuantity(max);
        return addOn;
    }

    @Test
    void priceAndDurationAreUnitTimesQuantity() {
        AddOn glitter = addOn(1, "Manikyr", null, 15, 1, 10);
        when(repository.findAllById(java.util.Set.of(1L))).thenReturn(List.of(glitter));

        List<ResolvedAddOn> result = addOnService.resolve(
                List.of(new AddOnSelection(1L, 4)), List.of(treatment("Manikyr", "Nytt sett")));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().totalPriceNok()).isEqualTo(60);
        assertThat(result.getFirst().totalDurationMinutes()).isEqualTo(4);
    }

    @Test
    void noExtrasChosenMeansNothingToResolve() {
        assertThat(addOnService.resolve(null, List.of(treatment("Manikyr", "Nytt sett")))).isEmpty();
        assertThat(addOnService.resolve(List.of(), List.of(treatment("Manikyr", "Nytt sett")))).isEmpty();
    }

    @Test
    void anExtraForManicureDoesNotFitAPedicure() {
        AddOn glitter = addOn(1, "Manikyr", null, 15, 1, 10);
        when(repository.findAllById(java.util.Set.of(1L))).thenReturn(List.of(glitter));

        assertThatThrownBy(() -> addOnService.resolve(
                List.of(new AddOnSelection(1L, 2)), List.of(treatment("Pedikyr", null))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("cannot be added");
    }

    @Test
    void shapeChangeIsOnlyForARefill() {
        assertThat(AddOnService.fits(addOn(5, "Manikyr", "Påfyll", 100, 15, 1), List.of(treatment("Manikyr", "Påfyll"))))
                .isTrue();
        assertThat(AddOnService.fits(addOn(5, "Manikyr", "Påfyll", 100, 15, 1), List.of(treatment("Manikyr", "Nytt sett"))))
                .isFalse();
    }

    @Test
    void anExtraFitsIfAnyTreatmentInTheCartMatches() {
        AddOn glitter = addOn(1, "Manikyr", null, 15, 1, 10);

        assertThat(AddOnService.fits(glitter, List.of(treatment("Pedikyr", null), treatment("Manikyr", "Påfyll"))))
                .isTrue();
    }

    @Test
    void moreThanTheMaximumIsRejected() {
        AddOn largeNails = addOn(4, "Manikyr", null, 100, 15, 1);
        when(repository.findAllById(java.util.Set.of(4L))).thenReturn(List.of(largeNails));

        assertThatThrownBy(() -> addOnService.resolve(
                List.of(new AddOnSelection(4L, 2)), List.of(treatment("Manikyr", "Nytt sett"))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("quantity");
    }

    @Test
    void anInactiveExtraCannotBeBooked() {
        AddOn glitter = addOn(1, "Manikyr", null, 15, 1, 10);
        glitter.setActive(false);
        when(repository.findAllById(java.util.Set.of(1L))).thenReturn(List.of(glitter));

        assertThatThrownBy(() -> addOnService.resolve(
                List.of(new AddOnSelection(1L, 1)), List.of(treatment("Manikyr", "Nytt sett"))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void theSameExtraTwiceIsRejected() {
        assertThatThrownBy(() -> addOnService.resolve(
                List.of(new AddOnSelection(1L, 1), new AddOnSelection(1L, 2)),
                List.of(treatment("Manikyr", "Nytt sett"))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("twice");
    }

    @Test
    void anUnknownExtraIsRejected() {
        when(repository.findAllById(java.util.Set.of(99L))).thenReturn(List.of());

        assertThatThrownBy(() -> addOnService.resolve(
                List.of(new AddOnSelection(99L, 1)), List.of(treatment("Manikyr", "Nytt sett"))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("do not exist");
    }
}
