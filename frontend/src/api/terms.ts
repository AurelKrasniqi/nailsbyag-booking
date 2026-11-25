// Mirrors TermsSectionResponse.java: one block of the "Salgsvilkår" page.
// heading is null for a block without a heading (the introduction). Paragraphs in body are separated by a blank line.
export interface TermsSection {
  heading: string | null
  body: string
}

export async function fetchTerms(): Promise<TermsSection[]> {
  const response = await fetch('/api/terms')
  if (!response.ok) {
    throw new Error(`Backend svarte med ${response.status}`)
  }
  return response.json()
}
