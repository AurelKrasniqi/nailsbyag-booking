package no.aurel.backend.accounting;

import java.io.ByteArrayOutputStream;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import no.aurel.backend.accounting.dto.AccountingLine;
import no.aurel.backend.accounting.dto.AccountingStatement;
import no.aurel.backend.appointment.PaymentMethod;
import no.aurel.backend.income.dto.MonthIncome;

// Only draws. It gets a finished AccountingStatement and decides nothing about the numbers.
@Component
public class PdfStatementWriter {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final Locale NORWEGIAN = Locale.forLanguageTag("nb-NO");

    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font HEADING = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font BODY = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font SMALL = FontFactory.getFont(FontFactory.HELVETICA, 8);

    private final String tradeName;
    private final String legalName;
    private final String orgNumber;
    private final String address;

    public PdfStatementWriter(
            @Value("${app.business.trade-name}") String tradeName,
            @Value("${app.business.legal-name}") String legalName,
            @Value("${app.business.org-number}") String orgNumber,
            @Value("${app.business.address}") String address) {
        this.tradeName = tradeName;
        this.legalName = legalName;
        this.orgNumber = orgNumber;
        this.address = address;
    }

    public byte[] write(AccountingStatement statement) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            document.addTitle("Regnskapsoversikt " + statement.from() + " til " + statement.to());
            document.addAuthor(tradeName);

            document.add(new Paragraph("Regnskapsoversikt", TITLE));
            document.add(new Paragraph(legalName + " (org.nr. " + orgNumber + ") · " + tradeName, BODY));
            document.add(new Paragraph(address, BODY));
            Paragraph period = new Paragraph(
                    "Periode: " + DATE.format(statement.from()) + " – " + DATE.format(statement.to()), HEADING);
            period.setSpacingBefore(10);
            document.add(period);
            document.add(new Paragraph(
                    statement.vatRegistered()
                            ? "Beløp er oppgitt i kroner inkludert " + statement.vatRatePercent() + " % merverdiavgift."
                            : "Foretaket er ikke registrert i MVA-registeret. Beløp er oppgitt i kroner.",
                    SMALL));

            document.add(heading("Sammendrag"));
            document.add(summaryTable(statement));

            if (statement.months().size() > 1) {
                document.add(heading("Per måned"));
                document.add(monthTable(statement));
            }

            document.add(heading("Timer i perioden"));
            document.add(linesTable(statement));

            String feeNote = statement.vippsGrossOre() > 0
                    ? " Vipps-gebyret er et estimat (" + feeRate(statement) + " per betaling). Det er en kostnad og er"
                            + " ikke trukket fra inntekt eller MVA-grunnlag. Vipps' oppgjørsrapporter viser faktiske beløp."
                    : "";
            Paragraph note = new Paragraph(
                    "Medregnet er timer som er gjennomført og ikke avbestilt. Kommende timer er ikke med." + feeNote, SMALL);
            note.setSpacingBefore(8);
            document.add(note);
        } catch (DocumentException e) {
            throw new IllegalStateException("Could not create the PDF", e);
        } finally {
            document.close();
        }
        return out.toByteArray();
    }

    private static Paragraph heading(String text) {
        Paragraph paragraph = new Paragraph(text, HEADING);
        paragraph.setSpacingBefore(14);
        paragraph.setSpacingAfter(4);
        return paragraph;
    }

    private static PdfPTable summaryTable(AccountingStatement s) {
        PdfPTable table = new PdfPTable(new float[] {3, 2});
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        row(table, "Antall timer", String.valueOf(s.appointments()), false);
        row(table, "Innbetalt (brutto)", kr(s.grossOre()), false);
        if (s.vatRegistered()) {
            row(table, "Merverdiavgift (" + s.vatRatePercent() + " %)", kr(s.vatOre()), false);
        }
        row(table, s.vatRegistered() ? "Inntekt uten MVA" : "Inntekt", kr(s.netOre()), true);
        row(table, "Hvorav betalt med Vipps", kr(s.vippsGrossOre()), false);
        row(table, "Hvorav betalt i salongen", kr(s.salonGrossOre()), false);
        if (s.vippsGrossOre() > 0) {
            row(table, "Vipps-gebyr (estimert, " + feeRate(s) + ")", "-" + kr(s.vippsFeeOre()), false);
            row(table, "Utbetalt fra Vipps (estimert)", kr(s.vippsGrossOre() - s.vippsFeeOre()), false);
            row(table, "Inntekt etter Vipps-gebyr (estimert)", kr(s.netOre() - s.vippsFeeOre()), true);
        }
        return table;
    }

    private static PdfPTable monthTable(AccountingStatement s) {
        PdfPTable table = new PdfPTable(new float[] {3, 1.5f, 2, 2, 2});
        table.setWidthPercentage(100);
        header(table, 1, "Måned", "Timer", "Brutto", "MVA", "Inntekt");
        for (MonthIncome m : s.months()) {
            cell(table, monthName(m.month()) + " " + m.year(), BODY, Element.ALIGN_LEFT);
            cell(table, String.valueOf(m.appointments()), BODY, Element.ALIGN_RIGHT);
            cell(table, kr(m.grossOre()), BODY, Element.ALIGN_RIGHT);
            cell(table, s.vatRegistered() ? kr(m.vatOre()) : "–", BODY, Element.ALIGN_RIGHT);
            cell(table, kr(m.netOre()), BODY, Element.ALIGN_RIGHT);
        }
        return table;
    }

    private static PdfPTable linesTable(AccountingStatement s) {
        PdfPTable table = new PdfPTable(new float[] {1.7f, 1, 6, 1.7f, 2});
        table.setWidthPercentage(100);
        table.setHeaderRows(1);
        header(table, 4, "Dato", "Tid", "Behandling", "Betaling", "Beløp");
        if (s.lines().isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("Ingen timer i denne perioden.", BODY));
            empty.setColspan(5);
            empty.setPadding(6);
            table.addCell(empty);
        }
        for (AccountingLine line : s.lines()) {
            cell(table, DATE.format(line.start()), BODY, Element.ALIGN_LEFT);
            cell(table, TIME.format(line.start()), BODY, Element.ALIGN_LEFT);
            cell(table, line.description(), BODY, Element.ALIGN_LEFT);
            cell(table, line.paymentMethod() == PaymentMethod.VIPPS ? "Vipps" : "Salong", BODY, Element.ALIGN_LEFT);
            cell(table, kr(line.grossOre()), BODY, Element.ALIGN_RIGHT);
        }
        PdfPCell total = new PdfPCell(new Phrase("Sum", BOLD));
        total.setColspan(4);
        total.setHorizontalAlignment(Element.ALIGN_RIGHT);
        total.setPadding(4);
        table.addCell(total);
        cell(table, kr(s.grossOre()), BOLD, Element.ALIGN_RIGHT);
        return table;
    }

    private static void row(PdfPTable table, String label, String value, boolean bold) {
        Font font = bold ? BOLD : BODY;
        PdfPCell left = new PdfPCell(new Phrase(label, font));
        PdfPCell right = new PdfPCell(new Phrase(value, font));
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        for (PdfPCell c : new PdfPCell[] {left, right}) {
            c.setBorder(Rectangle.BOTTOM);
            c.setPadding(4);
            table.addCell(c);
        }
    }

    // Columns from firstRightColumn onwards hold numbers, so their titles are right-aligned like the figures
    private static void header(PdfPTable table, int firstRightColumn, String... titles) {
        for (int i = 0; i < titles.length; i++) {
            PdfPCell c = new PdfPCell(new Phrase(titles[i], BOLD));
            c.setBackgroundColor(new java.awt.Color(235, 235, 235));
            c.setPadding(4);
            c.setHorizontalAlignment(i >= firstRightColumn ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
            table.addCell(c);
        }
    }

    private static void cell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setHorizontalAlignment(alignment);
        c.setPadding(4);
        c.setBorder(Rectangle.BOTTOM);
        table.addCell(c);
    }

    private static String monthName(int month) {
        String name = Month.of(month).getDisplayName(TextStyle.FULL, NORWEGIAN);
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    // "2,99 % + 1,00 kr"
    private static String feeRate(AccountingStatement s) {
        return String.format("%d,%02d %% + %s", s.feeBasisPoints() / 100, s.feeBasisPoints() % 100, kr(s.feeFixedOre()));
    }

    // 123456 øre -> "1 234,56 kr" (plain spaces: the PDF font cannot draw the non-breaking space Java would use)
    static String kr(long ore) {
        long abs = Math.abs(ore);
        String kroner = String.valueOf(abs / 100);
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < kroner.length(); i++) {
            if (i > 0 && (kroner.length() - i) % 3 == 0) grouped.append(' ');
            grouped.append(kroner.charAt(i));
        }
        return (ore < 0 ? "-" : "") + grouped + "," + String.format("%02d", abs % 100) + " kr";
    }
}
