import { useEffect, useState } from 'react'
import { fetchAvailability, type DayAvailability } from '../api/availability'
import { mondayBasedWeekday, monthRange, shortTime, toDateString } from '../dates'

const WEEKDAYS = ['Man', 'Tir', 'Ons', 'Tor', 'Fre', 'Lør', 'Søn']
const MONTH_FORMAT = new Intl.DateTimeFormat('nb-NO', { month: 'long', year: 'numeric' })
const DAY_FORMAT = new Intl.DateTimeFormat('nb-NO', { weekday: 'long', day: 'numeric', month: 'long' })

export interface Selection {
  date: string
  time: string
}

interface BookingCalendarProps {
  employeeId: number
  serviceIds: number[]
  selection: Selection | null
  onSelect: (selection: Selection | null) => void
  // Changing this number forces a fresh fetch (used after someone else took the time)
  reloadToken: number
}

// The result of one availability request. `key` says which request it belongs to, so a slow answer
// for an old month is never shown as if it were the current one.
interface LoadedMonth {
  key: string
  days: Map<string, string[]>
  error: string | null
}

function firstOfMonth(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), 1)
}

export function BookingCalendar({ employeeId, serviceIds, selection, onSelect, reloadToken }: BookingCalendarProps) {
  const [month, setMonth] = useState(() => firstOfMonth(new Date()))
  const [loaded, setLoaded] = useState<LoadedMonth | null>(null)

  const { from, to } = monthRange(month)
  const requestKey = `${employeeId}|${serviceIds.join(',')}|${from}|${reloadToken}`

  useEffect(() => {
    let cancelled = false
    fetchAvailability(employeeId, serviceIds, from, to)
      .then((result: DayAvailability[]) => {
        if (cancelled) return
        setLoaded({ key: requestKey, days: new Map(result.map((d) => [d.date, d.startTimes])), error: null })
      })
      .catch((e: Error) => {
        if (!cancelled) setLoaded({ key: requestKey, days: new Map(), error: e.message })
      })
    return () => {
      cancelled = true
    }
    // requestKey already covers employeeId, serviceIds, the month and the reload token
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [requestKey])

  const current = loaded?.key === requestKey ? loaded : null
  const loading = current === null
  const days = current?.days ?? new Map<string, string[]>()
  const error = current?.error ?? null

  const canGoBack = month.getTime() > firstOfMonth(new Date()).getTime()

  function changeMonth(delta: number) {
    setMonth(new Date(month.getFullYear(), month.getMonth() + delta, 1))
    onSelect(null)
  }

  const daysInMonth = new Date(month.getFullYear(), month.getMonth() + 1, 0).getDate()
  const cells: (number | null)[] = [
    ...Array<null>(mondayBasedWeekday(month)).fill(null),
    ...Array.from({ length: daysInMonth }, (_, i) => i + 1),
  ]

  const timesForSelected = selection ? (days.get(selection.date) ?? []) : []
  const monthHasSlots = [...days.values()].some((times) => times.length > 0)

  return (
    <>
      <div className="calendar-nav">
        <button type="button" className="link-button" disabled={!canGoBack} onClick={() => changeMonth(-1)}>
          ← Forrige
        </button>
        <strong className="calendar-title">{MONTH_FORMAT.format(month)}</strong>
        <button type="button" className="link-button" onClick={() => changeMonth(1)}>
          Neste →
        </button>
      </div>

      <div className="calendar-grid" aria-busy={loading}>
        {WEEKDAYS.map((w) => (
          <span key={w} className="calendar-weekday">
            {w}
          </span>
        ))}
        {cells.map((day, index) => {
          if (day === null) return <span key={`blank-${index}`} />
          const date = toDateString(new Date(month.getFullYear(), month.getMonth(), day))
          const available = (days.get(date)?.length ?? 0) > 0
          return (
            <button
              key={date}
              type="button"
              className={`calendar-day ${available ? 'available' : ''} ${selection?.date === date ? 'selected' : ''}`}
              disabled={!available}
              onClick={() => onSelect({ date, time: '' })}
            >
              {day}
            </button>
          )
        })}
      </div>

      {loading && <p className="status">Henter ledige tider…</p>}
      {error && <p className="error">Kunne ikke hente ledige tider: {error}</p>}
      {!loading && !error && !monthHasSlots && (
        <p className="status">Ingen ledige tider denne måneden. Prøv neste måned.</p>
      )}

      {selection && (
        <div className="time-picker">
          <h3>{DAY_FORMAT.format(new Date(`${selection.date}T00:00:00`))}</h3>
          <div className="time-grid">
            {timesForSelected.map((time) => (
              <button
                key={time}
                type="button"
                className={`time-slot ${selection.time === time ? 'selected' : ''}`}
                aria-pressed={selection.time === time}
                onClick={() => onSelect({ date: selection.date, time })}
              >
                {shortTime(time)}
              </button>
            ))}
          </div>
        </div>
      )}
    </>
  )
}
