// Mirrors ServiceGroupResponse.java: a box on the website that holds one or more treatments.
export interface ServiceGroup {
  id: number
  title: string
  // Small text above the title, e.g. "Manikyr" above "Nytt sett"
  eyebrow: string | null
  // The photo to show (the admin's own, otherwise the one that ships with the website). Null = none.
  imageUrl: string | null
  // True when the admin has uploaded her own photo
  hasUploadedImage: boolean
  // The photo that ships with the website, which comes back when the uploaded one is removed. Null = none.
  defaultImageUrl: string | null
  // Which part of a tall photo stays visible when it is cropped to the box: 0 = top, 100 = bottom
  imageFocus: number
  sortOrder: number
}

export async function fetchServiceGroups(): Promise<ServiceGroup[]> {
  const response = await fetch('/api/service-groups')
  if (!response.ok) {
    throw new Error(`Backend svarte med ${response.status}`)
  }
  return response.json()
}
