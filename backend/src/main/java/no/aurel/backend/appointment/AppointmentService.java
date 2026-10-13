package no.aurel.backend.appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.appointment.dto.AppointmentRequest;
import no.aurel.backend.appointment.dto.AppointmentResponse;
import no.aurel.backend.availability.AvailabilityService;
import no.aurel.backend.customer.Customer;
import no.aurel.backend.customer.CustomerRepository;
import no.aurel.backend.customer.dto.CustomerRequest;
import no.aurel.backend.employee.Employee;
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
    private final AvailabilityService availabilityService;

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
        // 1. Is the time actually offered? Same calculation the calendar uses, so the two can never disagree.
        //    It also rejects unknown employees/services and times in the past.
        requireAvailable(request);

        // 2. Load what the appointment is made of
        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee does not exist"));
        List<SalonService> services = salonServiceRepository.findAllById(new HashSet<>(request.serviceIds()));

        int totalMinutes = services.stream().mapToInt(SalonService::getDurationMinutes).sum();
        int totalPrice = services.stream().mapToInt(SalonService::getPriceNok).sum();

        Appointment appointment = new Appointment();
        appointment.setCustomer(findOrCreateCustomer(request.customer()));
        appointment.setEmployee(employee);
        appointment.setServices(new HashSet<>(services));
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(request.startTime().plusMinutes(totalMinutes));
        appointment.setTotalPriceNok(totalPrice);
        appointment.setStatus(AppointmentStatus.BOOKED);

        // 3. Save. If someone else booked the same time a moment ago, the database's exclusion constraint
        //    refuses the insert. saveAndFlush makes that happen here, where we can turn it into a clear 409.
        try {
            return AppointmentResponse.from(repository.saveAndFlush(appointment));
        } catch (DataIntegrityViolationException e) {
            throw timeTaken();
        }
    }

    public AppointmentResponse cancel(Long id) {
        Appointment appointment = getOrThrow(id);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return AppointmentResponse.from(appointment);
    }

    private void requireAvailable(AppointmentRequest request) {
        LocalDate date = request.startTime().toLocalDate();
        Set<LocalTime> offered = Set.copyOf(availabilityService
                .findAvailability(request.employeeId(), request.serviceIds(), date, date)
                .getFirst()
                .startTimes());
        if (!offered.contains(request.startTime().toLocalTime())) {
            throw timeTaken();
        }
    }

    // The same phone number is the same customer, so repeat customers do not pile up as duplicates
    private Customer findOrCreateCustomer(CustomerRequest details) {
        Customer customer = customerRepository.findFirstByPhone(details.phone()).orElseGet(Customer::new);
        customer.setPhone(details.phone());
        customer.setName(details.name());
        customer.setEmail(details.email());
        return customerRepository.save(customer);
    }

    private Appointment getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Appointment " + id + " not found"));
    }

    private ResponseStatusException timeTaken() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Tiden er ikke lenger ledig");
    }
}
