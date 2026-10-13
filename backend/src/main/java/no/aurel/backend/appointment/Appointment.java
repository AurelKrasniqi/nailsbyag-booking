package no.aurel.backend.appointment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.aurel.backend.customer.Customer;
import no.aurel.backend.employee.Employee;
import no.aurel.backend.salonservice.SalonService;

@Entity
@Table(name = "appointment")
@Getter
@Setter
@NoArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    // The treatments in the customer's cart, booked as one continuous appointment
    @ManyToMany
    @JoinTable(
            name = "appointment_service",
            joinColumns = @JoinColumn(name = "appointment_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id"))
    private Set<SalonService> services = new HashSet<>();

    // Extras the customer added (glitter, charms, ...), stored with their own copy of name and price
    @ElementCollection
    @CollectionTable(name = "appointment_add_on", joinColumns = @JoinColumn(name = "appointment_id"))
    private List<AppointmentAddOn> addOns = new ArrayList<>();

    // Stored at booking time so later price edits don't change existing appointments
    @Column(name = "total_price_nok", nullable = false)
    private int totalPriceNok;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    // Stored at booking time so later edits to the service's duration don't change existing appointments
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    // Optional note from the customer, written when booking
    @Column(name = "customer_comment", length = 500)
    private String customerComment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    // Random id handed to the customer; also the payment reference towards Vipps
    @Column(name = "public_token", nullable = false, unique = true, updatable = false, length = 36)
    private String publicToken;

    // For PENDING_PAYMENT: the time is released if the payment has not completed by then
    @Column(name = "payment_expires_at")
    private LocalDateTime paymentExpiresAt;
}
