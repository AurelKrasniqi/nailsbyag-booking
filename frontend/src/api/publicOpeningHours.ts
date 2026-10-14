import { fetchEmployees } from './availability'
import type { OpeningHours } from './admin'

export interface HoursRow {
  days: string
  hours: string
}

const DAY_ORDER = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY']
const DAY_NAMES = ['Mandag', 'Tirsdag', 'Onsdag', 'Torsdag', 'Fredag', 'Lørdag', 'Søndag']

// The salon has one worker, so the footer shows the first employee's weekly hours.
export async function fetchSalonOpeningHours(): Promise<HoursRow[]> {
  const employees = await fetchEmployees()
  if (employees.length === 0) return []
  const response = await fetch(`/api/employees/${employees[0].id}/opening-hours`)
  if (!response.ok) {
    throw new Error(`Backend svarte med ${response.status}`)
  }
  return toRows(await response.json())
}

// "10:00:00" -> "10:00"
function shortTime(time: string): string {
  return time.slice(0, 5)
}

// One text per weekday ("08:00 – 16:00", or "Stengt"), then neighbouring days with the same text are merged
// into one row ("Mandag – Fredag").
export function toRows(hours: OpeningHours[]): HoursRow[] {
  const textPerDay = DAY_ORDER.map((day) => {
    const intervals = hours
      .filter((h) => h.dayOfWeek === day)
      .sort((a, b) => a.startTime.localeCompare(b.startTime))
      .map((h) => `${shortTime(h.startTime)} – ${shortTime(h.endTime)}`)
    return intervals.length > 0 ? intervals.join(', ') : 'Stengt'
  })

  const rows: HoursRow[] = []
  let start = 0
  for (let i = 1; i <= textPerDay.length; i++) {
    if (i === textPerDay.length || textPerDay[i] !== textPerDay[start]) {
      const days = i - 1 === start ? DAY_NAMES[start] : `${DAY_NAMES[start]} – ${DAY_NAMES[i - 1]}`
      rows.push({ days, hours: textPerDay[start] })
      start = i
    }
  }
  return rows
}
