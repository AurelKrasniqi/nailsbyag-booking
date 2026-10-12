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
}

export async function fetchSalonServices(): Promise<SalonService[]> {
  const response = await fetch('/api/services')
  if (!response.ok) {
    throw new Error(`Backend svarte med ${response.status}`)
  }
  return response.json()
}
