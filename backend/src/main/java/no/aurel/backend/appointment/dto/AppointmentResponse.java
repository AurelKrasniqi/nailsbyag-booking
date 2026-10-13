package no.aurel.backend.appointment.dto;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import no.aurel.backend.appointment.Appointment;
import no.aurel.backend.appointment.AppointmentStatus;
import no.aurel.backend.appointment.PaymentMethod;
import no.aurel.backend.appointment.PaymentStatus;
import no.aurel.backend.salonservice.SalonService;

public record AppointmentResponse(
        Long id,
        Long customerId,
        String customerName,
        String customerPhone,
        Long employeeId,
        String employeeName,
        List<Long> serviceIds,
        List<String> serviceNames,
        List<AddOnLine> addOns,
        int totalPriceNok,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String comment,
        String publicToken,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        AppointmentStatus status
) {

    // One extra on the appointment: how many, and what that line cost in total
    public record AddOnLine(String name, int quantity, int priceNok) {
    }

    public static AppointmentResponse from(Appointment entity) {
        List<SalonService> services = entity.getServices().stream()
                .sorted(Comparator.comparing(SalonService::getId))
                .toList();
        return new AppointmentResponse(
                entity.getId(),
                entity.getCustomer().getId(),
                entity.getCustomer().getName(),
                entity.getCustomer().getPhone(),
                entity.getEmployee().getId(),
                entity.getEmployee().getName(),
                services.stream().map(SalonService::getId).toList(),
                services.stream().map(SalonService::getName).toList(),
                entity.getAddOns().stream()
                        .map(a -> new AddOnLine(a.getName(), a.getQuantity(), a.getPriceNok()))
                        .toList(),
                entity.getTotalPriceNok(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getCustomerComment(),
                entity.getPublicToken(),
                entity.getPaymentMethod(),
                entity.getPaymentStatus(),
                entity.getStatus()
        );
    }
}
