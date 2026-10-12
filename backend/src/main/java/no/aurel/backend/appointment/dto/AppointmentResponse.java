package no.aurel.backend.appointment.dto;

import java.time.LocalDateTime;

import no.aurel.backend.appointment.Appointment;
import no.aurel.backend.appointment.AppointmentStatus;

public record AppointmentResponse(
        Long id,
        Long customerId,
        String customerName,
        Long employeeId,
        String employeeName,
        Long serviceId,
        String serviceName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        AppointmentStatus status
) {

    public static AppointmentResponse from(Appointment entity) {
        return new AppointmentResponse(
                entity.getId(),
                entity.getCustomer().getId(),
                entity.getCustomer().getName(),
                entity.getEmployee().getId(),
                entity.getEmployee().getName(),
                entity.getService().getId(),
                entity.getService().getName(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getStatus()
        );
    }
}
