import type { SalonService } from './api/salonServices'

// Pure helpers for the selected treatments, so the totals can be reused by the cart bar and later by the booking step.
export function totalPriceNok(items: SalonService[]): number {
  return items.reduce((sum, s) => sum + s.priceNok, 0)
}

export function totalDurationMinutes(items: SalonService[]): number {
  return items.reduce((sum, s) => sum + s.durationMinutes, 0)
}

export function formatDuration(minutes: number): string {
  const hours = Math.floor(minutes / 60)
  const rest = minutes % 60
  if (hours === 0) return `${rest} min`
  return rest === 0 ? `${hours} t` : `${hours} t ${rest} min`
}
