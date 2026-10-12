package no.aurel.backend.appointment;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.appointment.dto.AppointmentRequest;
import no.aurel.backend.appointment.dto.AppointmentResponse;
import no.aurel.backend.customer.CustomerRepository;
import no.aurel.backend.employee.EmployeeRepository;
import no.aurel.backend.salonservice.SalonService;
import no.aurel.backend.salonservice.SalonServiceRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {

    private final AppointmentRepository repository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final SalonServiceRepository salonServiceRepository;

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findAll() {
        return repository.findAll(Sort.by("startTime")).stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponse findById(Long id) {
        return AppointmentResponse.from(getOrThrow(id));
    }

    public AppointmentResponse create(AppointmentRequest request) {
        Appointment appointment = new Appointment();
        appointment.setCustomer(customerRepository.findById(request.customerId())
                .orElseThrow(() -> badReference("Customer", request.customerId())));
        appointment.setEmployee(employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> badReference("Employee", request.employeeId())));
        SalonService salonService = salonServiceRepository.findById(request.serviceId())
                .orElseThrow(() -> badReference("Service", request.serviceId()));
        appointment.setService(salonService);
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(request.startTime().plusMinutes(salonService.getDurationMinutes()));
        appointment.setStatus(AppointmentStatus.BOOKED);
        return AppointmentResponse.from(repository.save(appointment));
    }

    public AppointmentResponse cancel(Long id) {
        Appointment appointment = getOrThrow(id);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return AppointmentResponse.from(appointment);
    }

    private Appointment getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Appointment " + id + " not found"));
    }

    private ResponseStatusException badReference(String type, Long id) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, type + " " + id + " does not exist");
    }
}
