export interface ServiceImage {
  src: string
  // CSS object-position: which part of the photo stays visible when it is cropped to the card (default "center 40%")
  position?: string
}

// Image for each service card. Put the files in frontend/public/images/services/ and add a line here.
// The key is the card's key from groupServices.ts: "Category|Subcategory" (subcategory empty if none),
// or just the service name for standalone services. Cards without an entry show a placeholder.
export const serviceImages: Record<string, ServiceImage> = {
  'Manikyr|Nytt sett': { src: '/images/services/nytt-sett.png', position: 'center 50%' },
  'Manikyr|Påfyll': { src: '/images/services/pafyll.jpg', position: 'center 65%' },
  'Pedikyr|': { src: '/images/services/pedikyr.jpg', position: 'center 65%' },
  Lashlift: { src: '/images/services/lashlift.jpg' },
  // No photos yet:
  // 'Microblading|': { src: '/images/services/microblading.jpg' },
  // Browlamination: { src: '/images/services/browlamination.jpg' },
}
