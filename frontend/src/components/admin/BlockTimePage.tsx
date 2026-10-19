import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
  createException,
  deleteException,
  fetchAppointments,
  fetchExceptions,
  fetchOpeningHours,
  UnauthorizedError,
  type AdminAppointment,
  type OpeningHours,
  type ScheduleException,
} from '../../api/admin'
import { mondayBasedWeekday, toDateString } from '../../dates'
import { BlockDayPanel } from './BlockDayPanel'
import { buildSlots, formatMinutes, summarizeDay } from './blockTimeModel'
import './BlockTime.css'

const MONTH_LABEL = new Intl.DateTimeFormat('nb-NO', { month: 'long', year: 'numeric' })
const WEEKDAY_LABELS = ['ma', 'ti', 'on', 'to', 'fr', 'lø', 'sø']
const MAX_MONTHS_AHEAD = 12

interface Toast {
  id: number
  message: string
  undo?: () => Promise<void>
}

interface Props {
  employeeId: number
  onUnauthorized: () => void
}

// An Airbnb-style month calendar: pick a day, then tap the times on that day to block them.
export function BlockTimePage({ employeeId, onUnauthorized }: Props) {
  const today = toDateString(new Date())
  const todayMonth = useMemo(() => new Date(new Date().getFullYear(), new Date().getMonth(), 1), [])

  const [hours, setHours] = useState<OpeningHours[]>([])
  const [exceptions, setExceptions] = useState<ScheduleException[]>([])
  const [appointments, setAppointments] = useState<AdminAppointment[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  const [month, setMonth] = useState(todayMonth)
  const [selectedDate, setSelectedDate] = useState(today)
  const [toast, setToast] = useState<Toast | null>(null)
  const toastTimer = useRef<number | undefined>(undefined)
  const panelRef = useRef<HTMLDivElement>(null)

  const handleError = useCallback(
    (e: unknown) => {
      if (e instanceof UnauthorizedError) onUnauthorized()
      else setError((e as Error).message)
    },
    [onUnauthorized],
  )

  useEffect(() => {
    Promise.all([fetchOpeningHours(employeeId), fetchExceptions(employeeId), fetchAppointments()])
      .then(([h, ex, all]) => {
        setHours(h)
        setExceptions(ex)
        setAppointments(all.filter((a) => a.status === 'BOOKED'))
      })
      .catch(handleError)
      .finally(() => setLoading(false))
  }, [employeeId, handleError])

  useEffect(() => () => window.clearTimeout(toastTimer.current), [])

  const exceptionsByDate = useMemo(() => groupBy(exceptions, (e) => e.date), [exceptions])
  const appointmentsByDate = useMemo(() => groupBy(appointments, (a) => a.startTime.slice(0, 10)), [appointments])

  function showToast(message: string, undo?: () => Promise<void>) {
    window.clearTimeout(toastTimer.current)
    setToast({ id: Date.now(), message, undo })
    toastTimer.current = window.setTimeout(() => setToast(null), 6000)
  }

  function addException(created: ScheduleException) {
    setExceptions((current) => [...current, created])
  }

  function removeException(id: number) {
    setExceptions((current) => current.filter((e) => e.id !== id))
  }

  // Creates a block and offers "Angre", which simply deletes it again
  async function block(date: string, range: { start: number; end: number } | null) {
    setSaving(true)
    setError(null)
    try {
      const created = await createException(employeeId, {
        date,
        type: 'BLOCKED',
        ...(range ? { startTime: formatMinutes(range.start), endTime: formatMinutes(range.end) } : {}),
      })
      addException(created)
      const what = range ? `${formatMinutes(range.start)}–${formatMinutes(range.end)}` : 'hele dagen'
      showToast(`Blokkert ${what}`, async () => {
        await deleteException(employeeId, created.id)
        removeException(created.id)
      })
    } catch (e) {
      handleError(e)
    } finally {
      setSaving(false)
    }
  }

  // Removes a block and offers "Angre", which creates it again
  async function unblock(blockToRemove: ScheduleException) {
    setSaving(true)
    setError(null)
    try {
      await deleteException(employeeId, blockToRemove.id)
      removeException(blockToRemove.id)
      showToast('Tiden er åpnet igjen', async () => {
        const restored = await createException(employeeId, {
          date: blockToRemove.date,
          type: 'BLOCKED',
          ...(blockToRemove.startTime && blockToRemove.endTime
            ? { startTime: blockToRemove.startTime.slice(0, 5), endTime: blockToRemove.endTime.slice(0, 5) }
            : {}),
        })
        addException(restored)
      })
    } catch (e) {
      handleError(e)
    } finally {
      setSaving(false)
    }
  }

  async function undo() {
    const action = toast?.undo
    window.clearTimeout(toastTimer.current)
    setToast(null)
    if (!action) return
    try {
      await action()
    } catch (e) {
      handleError(e)
    }
  }

  function selectDate(date: string) {
    setSelectedDate(date)
    // On a phone the day panel sits below the calendar: bring it into view
    if (window.matchMedia('(max-width: 859px)').matches) {
      window.setTimeout(() => panelRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 60)
    }
  }

  function changeMonth(delta: number) {
    setMonth((current) => new Date(current.getFullYear(), current.getMonth() + delta, 1))
  }

  const monthsAhead = (month.getFullYear() - todayMonth.getFullYear()) * 12 + month.getMonth() - todayMonth.getMonth()

  // Month grid: empty cells before the 1st so the weekdays line up, then one cell per day
  const daysInMonth = new Date(month.getFullYear(), month.getMonth() + 1, 0).getDate()
  const leadingBlanks = mondayBasedWeekday(month)
  const cells: (string | null)[] = [
    ...Array<null>(leadingBlanks).fill(null),
    ...Array.from({ length: daysInMonth }, (_, i) => toDateString(new Date(month.getFullYear(), month.getMonth(), i + 1))),
  ]

  const dayExceptions = exceptionsByDate.get(selectedDate) ?? []
  const dayAppointments = appointmentsByDate.get(selectedDate) ?? []
  const isPastDay = selectedDate < today
  const nowMinutes = selectedDate === today ? new Date().getHours() * 60 + new Date().getMinutes() : null
  const slots = buildSlots(selectedDate, hours, dayExceptions, dayAppointments, nowMinutes)
  const wholeDayBlock = dayExceptions.find((e) => e.type === 'BLOCKED' && !e.startTime)

  return (
    <div className="block-layout">
      <section className="admin-card block-calendar">
        <div className="month-nav">
          <button type="button" className="round-button" aria-label="Forrige måned" disabled={monthsAhead <= 0} onClick={() => changeMonth(-1)}>
            ‹
          </button>
          <h2 className="month-title">{MONTH_LABEL.format(month)}</h2>
          <button
            type="button"
            className="round-button"
            aria-label="Neste måned"
            disabled={monthsAhead >= MAX_MONTHS_AHEAD}
            onClick={() => changeMonth(1)}
          >
            ›
          </button>
        </div>

        {error && <p className="error">{error}</p>}

        <div className="month-grid" role="grid" aria-label={MONTH_LABEL.format(month)}>
          {WEEKDAY_LABELS.map((label) => (
            <span key={label} className="month-weekday" aria-hidden="true">
              {label}
            </span>
          ))}
          {cells.map((date, i) => {
            if (!date) return <span key={`blank-${i}`} />
            const summary = summarizeDay(date, hours, exceptionsByDate.get(date) ?? [], appointmentsByDate.get(date) ?? [])
            const past = date < today
            let classes = 'day-cell'
            if (date === selectedDate) classes += ' day-selected'
            if (date === today) classes += ' day-today'
            if (past) classes += ' day-past'
            if (summary.closed) classes += ' day-closed'
            if (summary.wholeDayBlock) classes += ' day-blocked'

            const parts = [
              summary.bookings > 0 ? `${summary.bookings} ${summary.bookings === 1 ? 'time' : 'timer'}` : null,
              summary.wholeDayBlock ? 'hele dagen blokkert' : summary.partialBlocks > 0 ? 'delvis blokkert' : null,
              summary.closed && !summary.wholeDayBlock ? 'stengt' : null,
            ].filter(Boolean)

            return (
              <button
                key={date}
                type="button"
                className={classes}
                aria-pressed={date === selectedDate}
                aria-label={`${Number(date.slice(8))}. ${MONTH_LABEL.format(month)}${parts.length ? ', ' + parts.join(', ') : ''}`}
                onClick={() => selectDate(date)}
              >
                <span className="day-number">{Number(date.slice(8))}</span>
                <span className="day-marks" aria-hidden="true">
                  {summary.bookings > 0 && <span className="mark mark-booking" />}
                  {!summary.wholeDayBlock && summary.partialBlocks > 0 && <span className="mark mark-block" />}
                </span>
              </button>
            )
          })}
        </div>

        <ul className="calendar-legend" aria-label="Forklaring">
          <li>
            <span className="mark mark-booking" /> Har timer
          </li>
          <li>
            <span className="mark mark-block" /> Delvis blokkert
          </li>
          <li>
            <span className="legend-swatch legend-blocked" /> Hele dagen blokkert
          </li>
        </ul>
      </section>

      <div ref={panelRef} className="block-panel-wrap">
        {loading ? (
          <section className="admin-card">
            <p className="status">Laster…</p>
          </section>
        ) : (
          <BlockDayPanel
            key={selectedDate}
            date={selectedDate}
            slots={slots}
            isPastDay={isPastDay}
            wholeDayBlock={wholeDayBlock}
            saving={saving}
            onBlockRange={(start, end) => block(selectedDate, { start, end })}
            onBlockWholeDay={() => block(selectedDate, null)}
            onUnblock={unblock}
          />
        )}
      </div>

      {toast && (
        <div key={toast.id} className="toast" role="status" aria-live="polite">
          <span>{toast.message}</span>
          {toast.undo && (
            <button type="button" className="toast-undo" onClick={undo}>
              Angre
            </button>
          )}
        </div>
      )}
    </div>
  )
}

function groupBy<T>(items: T[], key: (item: T) => string): Map<string, T[]> {
  const map = new Map<string, T[]>()
  for (const item of items) {
    const k = key(item)
    const list = map.get(k)
    if (list) list.push(item)
    else map.set(k, [item])
  }
  return map
}
