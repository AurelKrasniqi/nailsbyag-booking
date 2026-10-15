// Mirrors AboutResponse.java: the "Om oss" box. Paragraphs in body are separated by a blank line.
export interface About {
  heading: string
  body: string
  // Null = the default photo that ships with the website
  imageUrl: string | null
}

export const DEFAULT_ABOUT_IMAGE = '/images/om-oss.jpg'

export async function fetchAbout(): Promise<About> {
  const response = await fetch('/api/about')
  if (!response.ok) {
    throw new Error(`Backend svarte med ${response.status}`)
  }
  return response.json()
}
