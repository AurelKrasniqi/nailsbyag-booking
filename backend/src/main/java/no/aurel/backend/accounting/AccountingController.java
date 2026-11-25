package no.aurel.backend.accounting;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.accounting.dto.AccountingStatement;

// Admin only (everything under /api/admin needs the ADMIN role, see SecurityConfig)
@RestController
@RequestMapping("/api/admin/accounting")
@RequiredArgsConstructor
public class AccountingController {

    private final AccountingService service;
    private final PdfStatementWriter pdfWriter;

    // /api/admin/accounting/statement.pdf?from=2026-11-01&to=2026-11-30&newestFirst=false
    @GetMapping(value = "/statement.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> statementPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "false") boolean newestFirst) {
        AccountingStatement statement = service.statement(from, to, newestFirst);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("regnskap-" + from + "-til-" + to + ".pdf").build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfWriter.write(statement));
    }
}
