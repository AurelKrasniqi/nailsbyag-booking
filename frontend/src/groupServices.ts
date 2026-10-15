import type { SalonService } from './api/salonServices'
import type { ServiceGroup } from './api/serviceGroups'

// One box on the page. A box with several options lists them as rows; a box with one option is a plain service.
export interface ServiceCardData {
  groupId: number
  // Small label above the title, e.g. "Manikyr" above "Nytt sett"
  eyebrow: string | null
  title: string
  imageUrl: string | null
  imageFocus: number
  options: SalonService[]
}

// Puts each treatment in its box. The boxes keep the order they came in (the admin decides it); a box without
// treatments is left out unless keepEmpty is set (the admin needs to see those).
export function groupServices(
  groups: ServiceGroup[],
  services: SalonService[],
  keepEmpty = false,
): ServiceCardData[] {
  return groups
    .map((group) => ({
      groupId: group.id,
      eyebrow: group.eyebrow,
      title: group.title,
      imageUrl: group.imageUrl,
      imageFocus: group.imageFocus,
      options: services.filter((s) => s.groupId === group.id),
    }))
    .filter((card) => keepEmpty || card.options.length > 0)
}
