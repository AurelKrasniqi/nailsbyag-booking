// Mirrors SalonServiceResponse.java
export interface SalonService {
  id: number
  name: string
  description: string | null
  category: string | null
  subcategory: string | null
  durationMinutes: number
  priceNok: number
  active: boolean
  // Small print under the treatment, e.g. "Tillegg kan forekomme"
  note: string | null
  // No fixed price: shown as "kontakt oss" and cannot be booked online
  priceOnRequest: boolean
  // The box on the website the treatment is listed in
  groupId: number
}

export async function fetchSalonServices(): Promise<SalonService[]> {
  const response = await fetch('/api/services')
  if (!response.ok) {
    throw new Error(`Backend svarte med ${response.status}`)
  }
  return response.json()
}
