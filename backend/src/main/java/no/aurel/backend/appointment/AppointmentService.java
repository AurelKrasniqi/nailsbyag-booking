package no.aurel.backend.appointment;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.addon.AddOnService;
import no.aurel.backend.addon.ResolvedAddOn;
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

// Everything that changes an appointment in the database. Talking to the payment provider is NOT done here
// (see BookingService / PaymentService): a database transaction should not stay open while waiting for an
// external web service.
@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {

    private final AppointmentRepository repository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final SalonServiceRepository salonServiceRepository;
    private final AvailabilityService availabilityService;
    private final AddOnService addOnService;
    private final Clock clock;

    // How long a time is held for an unfinished online payment
    @Value("${app.payment.hold-minutes:10}")
    private int holdMinutes;

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

    @Transactional(readOnly = true)
    public AppointmentResponse findByPublicToken(String token) {
        return AppointmentResponse.from(getByTokenOrThrow(token));
    }

    @Transactional(readOnly = true)
    public List<String> findExpiredPendingTokens() {
        return repository.findByStatusAndPaymentExpiresAtBefore(
                        AppointmentStatus.PENDING_PAYMENT, LocalDateTime.now(clock)).stream()
                .map(Appointment::getPublicToken)
                .toList();
    }

    public AppointmentResponse create(AppointmentRequest request) {
        // 1. Is the time actually offered? Same calculation the calendar uses, so the two can never disagree.
        //    It also rejects unknown employees/services and times in the past.
        requireAvailable(request);

        // 2. Load what the appointment is made of
        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee does not exist"));
        List<SalonService> services = salonServiceRepository.findAllById(new HashSet<>(request.serviceIds()));

        // Extras are checked again here (never trust the browser): they must exist and fit the chosen treatments
        List<ResolvedAddOn> addOns = addOnService.resolve(request.addOns(), services);

        int totalMinutes = services.stream().mapToInt(SalonService::getDurationMinutes).sum()
                + addOns.stream().mapToInt(ResolvedAddOn::totalDurationMinutes).sum();
        int totalPrice = services.stream().mapToInt(SalonService::getPriceNok).sum()
                + addOns.stream().mapToInt(ResolvedAddOn::totalPriceNok).sum();

        Appointment appointment = new Appointment();
        appointment.setCustomer(findOrCreateCustomer(request.customer()));
        appointment.setEmployee(employee);
        appointment.setServices(new HashSet<>(services));
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(request.startTime().plusMinutes(totalMinutes));
        appointment.setTotalPriceNok(totalPrice);
        // Copy name, price and duration of each extra, so later price edits do not change this appointment
        appointment.setAddOns(addOns.stream()
                .map(a -> new AppointmentAddOn(a.addOn().getId(), a.addOn().getName(), a.quantity(),
                        a.totalPriceNok(), a.totalDurationMinutes()))
                .toList());
        // An empty or whitespace-only note is stored as "no comment"
        appointment.setCustomerComment(
                request.comment() == null || request.comment().isBlank() ? null : request.comment().trim());
        appointment.setPublicToken(UUID.randomUUID().toString());

        PaymentMethod method = request.paymentMethod() == null ? PaymentMethod.AT_SALON : request.paymentMethod();
        appointment.setPaymentMethod(method);
        if (method == PaymentMethod.VIPPS) {
            // Held, not confirmed: becomes BOOKED when Vipps confirms the payment, otherwise released
            appointment.setStatus(AppointmentStatus.PENDING_PAYMENT);
            appointment.setPaymentStatus(PaymentStatus.PENDING);
            appointment.setPaymentExpiresAt(LocalDateTime.now(clock).plusMinutes(holdMinutes));
        } else {
            appointment.setStatus(AppointmentStatus.BOOKED);
            appointment.setPaymentStatus(PaymentStatus.UNPAID);
        }

        // 3. Save. If someone else booked the same time a moment ago, the database's exclusion constraint
        //    refuses the insert. saveAndFlush makes that happen here, where we can turn it into a clear 409.
        try {
            return AppointmentResponse.from(repository.saveAndFlush(appointment));
        } catch (DataIntegrityViolationException e) {
            throw timeTaken();
        }
    }

    // The money was received: the held time becomes a confirmed booking. Safe to call twice.
    public void markPaid(String token) {
        Appointment appointment = getByTokenOrThrow(token);
        if (appointment.getStatus() == AppointmentStatus.PENDING_PAYMENT) {
            appointment.setStatus(AppointmentStatus.BOOKED);
            appointment.setPaymentStatus(PaymentStatus.PAID);
        }
    }

    // The payment failed or timed out: release the time. Only touches bookings still waiting for payment.
    public void markPaymentFailed(String token) {
        Appointment appointment = getByTokenOrThrow(token);
        if (appointment.getStatus() == AppointmentStatus.PENDING_PAYMENT) {
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointment.setPaymentStatus(PaymentStatus.FAILED);
        }
    }

    // refunded = the customer had paid online and the money has already been sent back
    public AppointmentResponse cancel(Long id, boolean refunded) {
        Appointment appointment = getOrThrow(id);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        if (refunded) {
            appointment.setPaymentStatus(PaymentStatus.REFUNDED);
        }
        return AppointmentResponse.from(appointment);
    }

    private void requireAvailable(AppointmentRequest request) {
        LocalDate date = request.startTime().toLocalDate();
        Set<LocalTime> offered = Set.copyOf(availabilityService
                .findAvailability(request.employeeId(), request.serviceIds(), request.addOns(), date, date)
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

    private Appointment getByTokenOrThrow(String token) {
        return repository.findByPublicToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }

    private ResponseStatusException timeTaken() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Tiden er ikke lenger ledig");
    }
}
