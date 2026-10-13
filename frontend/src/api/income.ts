import { adminFetch } from './admin'

// Mirrors MonthIncome.java. All money is in øre (1/100 kr) so no decimals can creep in.
//   grossOre: what the customers paid (prices include VAT)
//   vatOre:   the VAT part of that (0 if the business is not VAT registered)
//   netOre:   what is actually earned: gross minus VAT
export interface MonthIncome {
  year: number
  month: number
  appointments: number
  grossOre: number
  vatOre: number
  netOre: number
}

// Mirrors IncomeReport.java. `months` has one entry per month that has income, oldest first.
export interface IncomeReport {
  vatRegistered: boolean
  vatRatePercent: number
  months: MonthIncome[]
  // Booked but not yet performed: shown separately, not counted as income
  upcoming: { appointments: number; grossOre: number }
}

export interface Settings {
  vatRegistered: boolean
  vatRatePercent: number
}

export async function fetchIncome(): Promise<IncomeReport> {
  return (await adminFetch('/api/admin/income')).json()
}

export async function saveSettings(settings: Settings): Promise<Settings> {
  const response = await adminFetch('/api/admin/settings', { method: 'PUT', body: JSON.stringify(settings) })
  return response.json()
}
