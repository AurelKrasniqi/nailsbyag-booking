package no.aurel.backend.accounting.dto;

import java.time.LocalDate;
import java.util.List;

import no.aurel.backend.income.dto.MonthIncome;

// Everything the PDF shows for one period. All money is in øre.
//   months:      the totals per calendar month. VAT is worked out per month (as on the income page) and added up,
//                so the totals below are exactly the sum of the months.
//   vippsGrossOre / salonGrossOre: how much came in through Vipps and how much was paid in the salon
//   vippsFeeOre: estimated Vipps fee on those payments (a cost for the business, NOT taken off the income or the VAT).
//                feeBasisPoints (299 = 2,99 %) and feeFixedOre (per payment) are the rates used, so the PDF can state them.
public record AccountingStatement(
        LocalDate from,
        LocalDate to,
        boolean vatRegistered,
        int vatRatePercent,
        List<AccountingLine> lines,
        List<MonthIncome> months,
        long appointments,
        long grossOre,
        long vatOre,
        long netOre,
        long vippsGrossOre,
        long salonGrossOre,
        long vippsFeeOre,
        int feeBasisPoints,
        long feeFixedOre
) {
}
