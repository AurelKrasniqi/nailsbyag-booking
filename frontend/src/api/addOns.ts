// Mirrors AddOnResponse.java: an extra ("Tillegg") that can be added to a treatment, e.g. glitter or charms.
// Price and duration are for ONE unit (one nail, or the extra as a whole).
export interface AddOn {
  id: number
  name: string
  // What the price is per, e.g. "pr. negl". Null for a one-off extra.
  unitLabel: string | null
  category: string
  // Null = the extra fits every treatment in the category
  subcategory: string | null
  priceNok: number
  durationMinutes: number
  // 10 = chosen per nail, 1 = yes or no
  maxQuantity: number
}

export async function fetchAddOns(): Promise<AddOn[]> {
  const response = await fetch('/api/add-ons')
  if (!response.ok) {
    throw new Error(`Backend svarte med ${response.status}`)
  }
  return response.json()
}
