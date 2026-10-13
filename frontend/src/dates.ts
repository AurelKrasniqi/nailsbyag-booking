// Dates are handled as "YYYY-MM-DD" strings built from local parts. toISOString() converts to UTC and can shift the day.
export function toDateString(date: Date): string {
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

export function monthRange(monthStart: Date): { from: string; to: string } {
  const last = new Date(monthStart.getFullYear(), monthStart.getMonth() + 1, 0)
  return { from: toDateString(monthStart), to: toDateString(last) }
}

// "10:00:00" -> "10:00"
export function shortTime(time: string): string {
  return time.slice(0, 5)
}

// Monday = 0 ... Sunday = 6 (Norwegian weeks start on Monday)
export function mondayBasedWeekday(date: Date): number {
  return (date.getDay() + 6) % 7
}
