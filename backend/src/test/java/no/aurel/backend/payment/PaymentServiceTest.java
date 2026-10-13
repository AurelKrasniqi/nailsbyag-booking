package no.aurel.backend.payment;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
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

import no.aurel.backend.appointment.AppointmentService;
import no.aurel.backend.appointment.AppointmentStatus;
import no.aurel.backend.appointment.PaymentMethod;
import no.aurel.backend.appointment.PaymentStatus;
import no.aurel.backend.appointment.dto.AppointmentResponse;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final String TOKEN = "token-123";
    private static final int PRICE = 750;

    @Mock
    private PaymentGateway gateway;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private PaymentService paymentService;

    private static AppointmentResponse appointment(AppointmentStatus status) {
        return new AppointmentResponse(1L, 1L, "Kari", "40012345", 2L, "Agneta", List.of(10L), List.of("Pedikyr"), List.of(),
                PRICE, LocalDateTime.of(2026, 10, 20, 10, 0), LocalDateTime.of(2026, 10, 20, 11, 0), null, TOKEN,
                PaymentMethod.VIPPS, PaymentStatus.PENDING, status);
    }

    private void givenPendingBooking() {
        when(appointmentService.findByPublicToken(TOKEN)).thenReturn(appointment(AppointmentStatus.PENDING_PAYMENT));
    }

    @Test
    void customerHasNotPaidYetLeavesTheBookingAlone() {
        givenPendingBooking();
        when(gateway.getState(TOKEN)).thenReturn(PaymentGatewayState.PENDING);

        paymentService.check(TOKEN);

        verify(appointmentService, never()).markPaid(anyString());
        verify(appointmentService, never()).markPaymentFailed(anyString());
    }

    @Test
    void reservedMoneyIsCapturedBeforeTheBookingIsConfirmed() {
        givenPendingBooking();
        when(gateway.getState(TOKEN)).thenReturn(PaymentGatewayState.AUTHORIZED);

        paymentService.check(TOKEN);

        verify(gateway).capture(TOKEN, PRICE);
        verify(appointmentService).markPaid(TOKEN);
    }

    @Test
    void alreadyCapturedMoneyConfirmsWithoutCapturingTwice() {
        givenPendingBooking();
        when(gateway.getState(TOKEN)).thenReturn(PaymentGatewayState.CAPTURED);

        paymentService.check(TOKEN);

        verify(gateway, never()).capture(anyString(), anyInt());
        verify(appointmentService).markPaid(TOKEN);
    }

    @Test
    void rejectedPaymentReleasesTheTime() {
        givenPendingBooking();
        when(gateway.getState(TOKEN)).thenReturn(PaymentGatewayState.FAILED);

        paymentService.check(TOKEN);

        verify(appointmentService).markPaymentFailed(TOKEN);
        verify(appointmentService, never()).markPaid(anyString());
    }

    @Test
    void aBookingThatIsAlreadyConfirmedNeverCallsThePaymentProvider() {
        when(appointmentService.findByPublicToken(TOKEN)).thenReturn(appointment(AppointmentStatus.BOOKED));

        paymentService.check(TOKEN);

        verifyNoInteractions(gateway);
    }

    @Test
    void expiredUnpaidBookingIsCancelledAtTheProviderAndReleased() {
        givenPendingBooking();
        when(gateway.getState(TOKEN)).thenReturn(PaymentGatewayState.PENDING);

        paymentService.expire(TOKEN);

        verify(gateway).cancel(TOKEN);
        verify(appointmentService).markPaymentFailed(TOKEN);
    }

    @Test
    void expiredBookingThatWasPaidAtTheLastMomentIsKept() {
        givenPendingBooking();
        when(gateway.getState(TOKEN)).thenReturn(PaymentGatewayState.AUTHORIZED);

        paymentService.expire(TOKEN);

        verify(gateway).capture(TOKEN, PRICE);
        verify(appointmentService).markPaid(TOKEN);
        verify(gateway, never()).cancel(anyString());
        verify(appointmentService, never()).markPaymentFailed(anyString());
    }
}
