import { useEffect, useState } from 'react'
import { fetchOpeningHours, saveOpeningHours, UnauthorizedError, type DayOfWeek } from '../../api/admin'
import { shortTime } from '../../dates'

const DAYS: { key: DayOfWeek; label: string }[] = [
  { key: 'MONDAY', label: 'Mandag' },
  { key: 'TUESDAY', label: 'Tirsdag' },
  { key: 'WEDNESDAY', label: 'Onsdag' },
  { key: 'THURSDAY', label: 'Torsdag' },
  { key: 'FRIDAY', label: 'Fredag' },
  { key: 'SATURDAY', label: 'Lørdag' },
  { key: 'SUNDAY', label: 'Søndag' },
]

interface Interval {
  start: string
  end: string
}

type Week = Record<DayOfWeek, Interval[]>

const EMPTY_WEEK: Week = {
  MONDAY: [],
  TUESDAY: [],
  WEDNESDAY: [],
  THURSDAY: [],
  FRIDAY: [],
  SATURDAY: [],
  SUNDAY: [],
}

interface Props {
  employeeId: number
  onUnauthorized: () => void
}

// The weekly opening hours. A day without any interval is closed. Saving sends the whole week at once.
export function OpeningHoursEditor({ employeeId, onUnauthorized }: Props) {
  const [week, setWeek] = useState<Week>(EMPTY_WEEK)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState<{ text: string; error: boolean } | null>(null)

  useEffect(() => {
    fetchOpeningHours(employeeId)
      .then((hours) => {
        const loaded: Week = {
          MONDAY: [], TUESDAY: [], WEDNESDAY: [], THURSDAY: [], FRIDAY: [], SATURDAY: [], SUNDAY: [],
        }
        for (const h of hours) loaded[h.dayOfWeek].push({ start: shortTime(h.startTime), end: shortTime(h.endTime) })
        setWeek(loaded)
      })
      .catch((e: Error) => {
        if (e instanceof UnauthorizedError) onUnauthorized()
        else setMessage({ text: e.message, error: true })
      })
      .finally(() => setLoading(false))
  }, [employeeId, onUnauthorized])

  function update(day: DayOfWeek, intervals: Interval[]) {
    setWeek((current) => ({ ...current, [day]: intervals }))
    setMessage(null)
  }

  async function save() {
    setSaving(true)
    setMessage(null)
    try {
      const body = DAYS.flatMap(({ key }) =>
        week[key].map((i) => ({ dayOfWeek: key, startTime: i.start, endTime: i.end })),
      )
      await saveOpeningHours(employeeId, body)
      setMessage({ text: 'Åpningstidene er lagret.', error: false })
    } catch (e) {
      if (e instanceof UnauthorizedError) onUnauthorized()
      else setMessage({ text: (e as Error).message, error: true })
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <p className="status">Laster åpningstider…</p>

  return (
    <section className="admin-card">
      <h2>Åpningstider</h2>
      <p className="admin-hint">Faste tider hver uke. Legg til to tidsrom på samme dag hvis du har pause midt på dagen.</p>

      {DAYS.map(({ key, label }) => (
        <div key={key} className="hours-row">
          <strong className="hours-day">{label}</strong>
          <div className="hours-intervals">
            {week[key].length === 0 && <span className="admin-muted">Stengt</span>}
            {week[key].map((interval, index) => (
              <div key={index} className="hours-interval">
                <input
                  type="time"
                  aria-label={`${label} fra`}
                  value={interval.start}
                  onChange={(e) =>
                    update(key, week[key].map((x, i) => (i === index ? { ...x, start: e.target.value } : x)))
                  }
                />
                <span>–</span>
                <input
                  type="time"
                  aria-label={`${label} til`}
                  value={interval.end}
                  onChange={(e) =>
                    update(key, week[key].map((x, i) => (i === index ? { ...x, end: e.target.value } : x)))
                  }
                />
                <button
                  type="button"
                  className="link-button"
                  onClick={() => update(key, week[key].filter((_, i) => i !== index))}
                >
                  Fjern
                </button>
              </div>
            ))}
            <button
              type="button"
              className="link-button"
              onClick={() => update(key, [...week[key], { start: '10:00', end: '18:00' }])}
            >
              + Legg til tidsrom
            </button>
          </div>
        </div>
      ))}

      {message && <p className={message.error ? 'error' : 'admin-success'}>{message.text}</p>}
      <button type="button" className="button admin-button" disabled={saving} onClick={save}>
        {saving ? 'Lagrer…' : 'Lagre åpningstider'}
      </button>
    </section>
  )
}
