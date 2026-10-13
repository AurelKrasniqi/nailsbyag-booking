package no.aurel.backend.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import no.aurel.backend.appointment.AppointmentService;
import no.aurel.backend.appointment.AppointmentStatus;
import no.aurel.backend.appointment.PaymentMethod;
import no.aurel.backend.appointment.PaymentStatus;
import no.aurel.backend.appointment.dto.AppointmentRequest;
import no.aurel.backend.appointment.dto.AppointmentResponse;
import no.aurel.backend.appointment.dto.BookingResult;
import no.aurel.backend.customer.dto.CustomerRequest;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private BookingService bookingService;

    private static AppointmentRequest request(PaymentMethod method) {
        return new AppointmentRequest(2L, List.of(10L), LocalDateTime.of(2026, 10, 20, 10, 0),
                new CustomerRequest("Kari", "40012345", null), null, method, null);
    }

    private static AppointmentResponse booked(PaymentMethod method) {
        return new AppointmentResponse(1L, 1L, "Kari", "40012345", 2L, "Agneta", List.of(10L), List.of("Pedikyr"),
                List.of(), 750, LocalDateTime.of(2026, 10, 20, 10, 0), LocalDateTime.of(2026, 10, 20, 11, 0), null,
                "token", method, PaymentStatus.UNPAID, AppointmentStatus.BOOKED);
    }

    @Test
    void onlinePaymentIsRefusedWhenItIsSwitchedOffAndNothingIsSaved() {
        when(paymentService.onlinePaymentEnabled()).thenReturn(false);

        assertThatThrownBy(() -> bookingService.book(request(PaymentMethod.VIPPS)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Nettbetaling");

        verifyNoInteractions(appointmentService);
    }

    @Test
    void payingAtTheSalonWorksEvenWhenOnlinePaymentIsOff() {
        when(appointmentService.create(any())).thenReturn(booked(PaymentMethod.AT_SALON));

        BookingResult result = bookingService.book(request(PaymentMethod.AT_SALON));

        assertThat(result.paymentRedirectUrl()).isNull();
        verify(appointmentService).create(any());
    }

    @Test
    void aBookingWithoutAPaymentMethodDefaultsToPayingAtTheSalon() {
        when(appointmentService.create(any())).thenReturn(booked(PaymentMethod.AT_SALON));

        BookingResult result = bookingService.book(request(null));

        assertThat(result.paymentRedirectUrl()).isNull();
    }
}
