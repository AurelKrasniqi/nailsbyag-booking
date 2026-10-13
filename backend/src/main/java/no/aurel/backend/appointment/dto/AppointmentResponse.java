package no.aurel.backend.appointment.dto;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import no.aurel.backend.appointment.Appointment;
import no.aurel.backend.appointment.AppointmentStatus;
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
        int totalPriceNok,
        LocalDateTime startTime,
        LocalDateTime endTime,
        AppointmentStatus status
) {

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
                entity.getTotalPriceNok(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getStatus()
        );
    }
}
