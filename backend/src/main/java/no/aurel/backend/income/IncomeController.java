package no.aurel.backend.income;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.income.dto.IncomeReport;

// Admin only (everything under /api/admin needs the ADMIN role, see SecurityConfig)
@RestController
@RequestMapping("/api/admin/income")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService service;

    @GetMapping
    public IncomeReport report() {
        return service.report();
    }
}
