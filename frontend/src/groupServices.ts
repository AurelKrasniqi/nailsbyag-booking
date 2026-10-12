import type { SalonService } from './api/salonServices'

// One card on the page. A card with several options lists them as rows; a card with one option is a plain service.
export interface ServiceCardData {
  // Stable identifier, also used to look up the card's image in serviceImages.ts
  key: string
  // Small label above the title, e.g. "Manikyr" above "Nytt sett"
  eyebrow: string | null
  title: string
  options: SalonService[]
}

// Services with a category share a card (one card per category + subcategory); the rest get a card each.
// Order follows the first appearance in the (id-sorted) API response.
export function groupServices(services: SalonService[]): ServiceCardData[] {
  const cards: ServiceCardData[] = []
  const byKey = new Map<string, ServiceCardData>()

  for (const service of services) {
    const key = service.category ? `${service.category}|${service.subcategory ?? ''}` : service.name

    let card = byKey.get(key)
    if (!card) {
      card = {
        key,
        eyebrow: service.category && service.subcategory ? service.category : null,
        title: service.category ? (service.subcategory ?? service.category) : service.name,
        options: [],
      }
      byKey.set(key, card)
      cards.push(card)
    }
    card.options.push(service)
  }

  return cards
}
