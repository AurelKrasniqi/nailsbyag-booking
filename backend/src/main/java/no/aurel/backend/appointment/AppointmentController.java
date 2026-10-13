package no.aurel.backend.appointment;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.aurel.backend.appointment.dto.AppointmentRequest;
import no.aurel.backend.appointment.dto.AppointmentResponse;
import no.aurel.backend.appointment.dto.BookingResult;
import no.aurel.backend.payment.BookingService;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService service;
    private final BookingService bookingService;

    @GetMapping
    public List<AppointmentResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public AppointmentResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<BookingResult> create(@Valid @RequestBody AppointmentRequest request) {
        BookingResult result = bookingService.book(request);
        return ResponseEntity.created(URI.create("/api/appointments/" + result.appointment().id())).body(result);
    }

    @PutMapping("/{id}/cancel")
    public AppointmentResponse cancel(@PathVariable Long id) {
        return bookingService.cancel(id);
    }
}
