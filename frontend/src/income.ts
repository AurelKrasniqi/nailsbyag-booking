import type { MonthIncome } from './api/income'

// Pure helpers for the income screens. Money is whole øre (integers), so adding months up is exact.
export interface Totals {
  appointments: number
  grossOre: number
  vatOre: number
  netOre: number
}

export type Period = 'month' | 'year' | 'all'

export function sumMonths(months: MonthIncome[]): Totals {
  return months.reduce<Totals>(
    (sum, m) => ({
      appointments: sum.appointments + m.appointments,
      grossOre: sum.grossOre + m.grossOre,
      vatOre: sum.vatOre + m.vatOre,
      netOre: sum.netOre + m.netOre,
    }),
    { appointments: 0, grossOre: 0, vatOre: 0, netOre: 0 },
  )
}

const KRONER = new Intl.NumberFormat('nb-NO', { style: 'currency', currency: 'NOK' })

// 123456 øre -> "1 234,56 kr"
export function formatKr(ore: number): string {
  return KRONER.format(ore / 100)
}

const MONTH_NAMES = new Intl.DateTimeFormat('nb-NO', { month: 'long' })

// 1..12 -> "oktober"
export function monthName(month: number): string {
  return MONTH_NAMES.format(new Date(2000, month - 1, 1))
}

export function emptyMonth(year: number, month: number): MonthIncome {
  return { year, month, appointments: 0, grossOre: 0, vatOre: 0, netOre: 0 }
}
