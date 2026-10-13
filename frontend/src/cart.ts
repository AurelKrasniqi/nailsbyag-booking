import type { AddOn } from './api/addOns'
import type { SalonService } from './api/salonServices'

// An extra the customer has chosen, and how many (e.g. glitter on 4 nails)
export interface AddOnChoice {
  addOn: AddOn
  quantity: number
}

// Pure helpers for the cart (chosen treatments plus extras), so the totals are the same in the cart bar,
// the booking dialog and the request sent to the backend.
export function totalPriceNok(items: SalonService[], choices: AddOnChoice[] = []): number {
  return (
    items.reduce((sum, s) => sum + s.priceNok, 0) +
    choices.reduce((sum, c) => sum + c.addOn.priceNok * c.quantity, 0)
  )
}

export function totalDurationMinutes(items: SalonService[], choices: AddOnChoice[] = []): number {
  return (
    items.reduce((sum, s) => sum + s.durationMinutes, 0) +
    choices.reduce((sum, c) => sum + c.addOn.durationMinutes * c.quantity, 0)
  )
}

// An extra fits when a chosen treatment is in its category (and subcategory, if the extra has one).
// Same rule as the backend, which checks it again on booking.
export function eligibleAddOns(all: AddOn[], items: SalonService[]): AddOn[] {
  return all.filter((a) =>
    items.some((s) => s.category === a.category && (a.subcategory === null || a.subcategory === s.subcategory)),
  )
}

// The quantities live in one place, keyed by extra id. Only extras that still fit the cart and have a quantity count,
// so removing a treatment automatically drops the extras that no longer fit.
export function chosenAddOns(eligible: AddOn[], quantities: Record<number, number>): AddOnChoice[] {
  return eligible
    .filter((a) => (quantities[a.id] ?? 0) > 0)
    .map((a) => ({ addOn: a, quantity: Math.min(quantities[a.id], a.maxQuantity) }))
}

export function formatDuration(minutes: number): string {
  const hours = Math.floor(minutes / 60)
  const rest = minutes % 60
  if (hours === 0) return `${rest} min`
  return rest === 0 ? `${hours} t` : `${hours} t ${rest} min`
}

// "15 kr pr. negl" or "100 kr"
export function formatAddOnPrice(addOn: AddOn): string {
  return `${addOn.priceNok} kr${addOn.unitLabel ? ` ${addOn.unitLabel}` : ''}`
}

// "Glitter/Chrome ×4" (just the name for a one-off extra)
export function formatChoice(choice: AddOnChoice): string {
  return choice.addOn.maxQuantity > 1 ? `${choice.addOn.name} ×${choice.quantity}` : choice.addOn.name
}
