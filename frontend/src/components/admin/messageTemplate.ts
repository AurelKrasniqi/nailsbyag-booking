// Helpers for the "Meldinger" page. The real filling-in happens in the backend (MessageTemplateRenderer.java);
// this mirrors it only to show Agneta a preview of what the customer will receive.

export const PLACEHOLDER_LABELS: Record<string, string> = {
  navn: 'Kundens fornavn',
  dato: 'Dato',
  tid: 'Klokkeslett',
  behandling: 'Behandling',
  pris: 'Pris',
}

const TOKEN = /\{([^{}]*)\}/g

export interface SampleBooking {
  navn: string
  dato: string
  tid: string
  behandling: string
  pris: string
}

export function sampleBooking(): SampleBooking {
  const inTwoDays = new Date()
  inTwoDays.setDate(inTwoDays.getDate() + 2)
  const pad = (n: number) => String(n).padStart(2, '0')
  return {
    navn: 'Kari',
    dato: `${pad(inTwoDays.getDate())}.${pad(inTwoDays.getMonth() + 1)}.${inTwoDays.getFullYear()}`,
    tid: '14:00',
    behandling: 'Gellack',
    pris: '750 kr',
  }
}

export function renderSample(template: string, sample: SampleBooking): string {
  return template.replace(TOKEN, (whole, name: string) => (name in sample ? sample[name as keyof SampleBooking] : whole))
}

// Placeholders the backend does not know, e.g. a typo like {dao}. The backend refuses to save them.
export function unknownPlaceholders(template: string, known: string[]): string[] {
  const unknown = new Set<string>()
  for (const match of template.matchAll(TOKEN)) {
    if (!known.includes(match[1])) unknown.add(match[1])
  }
  return [...unknown]
}

// Letters outside this set (emoji, for instance) make the whole SMS use a different encoding: 70 instead of 160
// characters per part. This is the standard GSM 03.38 alphabet, the ones we care about for Norwegian.
const GSM_CHARS =
  /^[A-Za-z0-9 \n\r@£$¥èéùìòÇØøÅåΔ_ΦΓΛΩΠΨΣΘΞÆæßÉ!"#¤%&'()*+,\-./:;<=>?¡ÄÖÑÜ§¿äöñüà{}[\]~|^€\\]*$/

export function smsInfo(text: string): { length: number; parts: number; unicode: boolean } {
  const unicode = !GSM_CHARS.test(text)
  const single = unicode ? 70 : 160
  const multi = unicode ? 67 : 153
  const length = [...text].length
  const parts = length <= single ? 1 : Math.ceil(length / multi)
  return { length, parts, unicode }
}
