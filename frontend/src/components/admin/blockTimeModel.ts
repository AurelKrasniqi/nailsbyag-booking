// Pure helpers for the "Blokker tid" page: turn opening hours, blocks and bookings into a list of 30 minute slots.
// No React in here, so the rules are easy to read (and to test).
import type { AdminAppointment, DayOfWeek, OpeningHours, ScheduleException } from '../../api/admin'
import { mondayBasedWeekday } from '../../dates'

export const STEP = 30

const WEEKDAYS: DayOfWeek[] = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY']

export type SlotState = 'closed' | 'past' | 'free' | 'booked' | 'blocked'

export interface Slot {
  start: number // minutes since midnight
  end: number
  state: SlotState
  appointment?: AdminAppointment
  block?: ScheduleException
}

// "10:00:00" or "10:00" -> 600
export function toMinutes(time: string): number {
  return Number(time.slice(0, 2)) * 60 + Number(time.slice(3, 5))
}

// 600 -> "10:00". A day cannot go past 23:59, so 24:00 is clamped.
export function formatMinutes(minutes: number): string {
  const clamped = Math.min(minutes, 24 * 60 - 1)
  const h = String(Math.floor(clamped / 60)).padStart(2, '0')
  const m = String(clamped % 60).padStart(2, '0')
  return `${h}:${m}`
}

// 90 -> "1 t 30 min"
export function formatDuration(minutes: number): string {
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  if (h === 0) return `${m} min`
  return m === 0 ? `${h} t` : `${h} t ${m} min`
}

// The intervals the salon is open on one date: the weekly hours plus any "ekstra åpent" for that date
export function openRanges(date: string, hours: OpeningHours[], dayExceptions: ScheduleException[]): [number, number][] {
  const weekday = WEEKDAYS[mondayBasedWeekday(new Date(`${date}T00:00:00`))]
  const ranges: [number, number][] = hours
    .filter((h) => h.dayOfWeek === weekday)
    .map((h) => [toMinutes(h.startTime), toMinutes(h.endTime)])
  for (const e of dayExceptions) {
    if (e.type === 'EXTRA_OPEN' && e.startTime && e.endTime) ranges.push([toMinutes(e.startTime), toMinutes(e.endTime)])
  }
  return ranges
}

const overlaps = (aStart: number, aEnd: number, bStart: number, bEnd: number) => aStart < bEnd && bStart < aEnd

// nowMinutes is only given for today: slots that have already started cannot be blocked any more
export function buildSlots(
  date: string,
  hours: OpeningHours[],
  dayExceptions: ScheduleException[],
  dayAppointments: AdminAppointment[],
  nowMinutes: number | null,
): Slot[] {
  const open = openRanges(date, hours, dayExceptions)
  const booked = dayAppointments.map((a) => ({
    appointment: a,
    start: toMinutes(a.startTime.slice(11)),
    end: a.endTime.slice(0, 10) > date ? 24 * 60 : toMinutes(a.endTime.slice(11)),
  }))
  const blocks = dayExceptions
    .filter((e) => e.type === 'BLOCKED')
    .map((e) => ({
      block: e,
      start: e.startTime ? toMinutes(e.startTime) : 0, // no times = the whole day
      end: e.endTime ? toMinutes(e.endTime) : 24 * 60,
    }))

  const edges = [...open, ...booked.map((b): [number, number] => [b.start, b.end])]
  if (edges.length === 0) return []
  const from = Math.floor(Math.min(...edges.map((r) => r[0])) / STEP) * STEP
  const to = Math.ceil(Math.max(...edges.map((r) => r[1])) / STEP) * STEP

  const slots: Slot[] = []
  for (let start = from; start < to; start += STEP) {
    const end = start + STEP
    const appointment = booked.find((b) => overlaps(start, end, b.start, b.end))
    const block = blocks.find((b) => overlaps(start, end, b.start, b.end))
    const isOpen = open.some(([s, e]) => overlaps(start, end, s, e))

    if (appointment) slots.push({ start, end, state: 'booked', appointment: appointment.appointment })
    else if (block) slots.push({ start, end, state: 'blocked', block: block.block })
    else if (!isOpen) slots.push({ start, end, state: 'closed' })
    else if (nowMinutes !== null && start < nowMinutes) slots.push({ start, end, state: 'past' })
    else slots.push({ start, end, state: 'free' })
  }
  return slots
}

// What the month grid shows for a day
export interface DaySummary {
  closed: boolean
  bookings: number
  wholeDayBlock: ScheduleException | undefined
  partialBlocks: number
}

export function summarizeDay(
  date: string,
  hours: OpeningHours[],
  dayExceptions: ScheduleException[],
  dayAppointments: AdminAppointment[],
): DaySummary {
  const blocked = dayExceptions.filter((e) => e.type === 'BLOCKED')
  return {
    closed: openRanges(date, hours, dayExceptions).length === 0,
    bookings: dayAppointments.length,
    wholeDayBlock: blocked.find((e) => !e.startTime),
    partialBlocks: blocked.filter((e) => e.startTime).length,
  }
}
