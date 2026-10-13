import { useEffect, useState, type FormEvent } from 'react'
import {
  createException,
  deleteException,
  fetchExceptions,
  UnauthorizedError,
  type ExceptionType,
  type ScheduleException,
} from '../../api/admin'
import { shortTime } from '../../dates'

const DATE_FORMAT = new Intl.DateTimeFormat('nb-NO', { weekday: 'short', day: 'numeric', month: 'long', year: 'numeric' })

interface Props {
  employeeId: number
  onUnauthorized: () => void
}

function describe(e: ScheduleException): string {
  const when = e.startTime && e.endTime ? `${shortTime(e.startTime)}–${shortTime(e.endTime)}` : 'hele dagen'
  return e.type === 'BLOCKED' ? `Stengt ${when}` : `Ekstra åpent ${when}`
}

// One-off changes: days off, a blocked afternoon, or an extra opening outside the weekly hours
export function ExceptionsEditor({ employeeId, onUnauthorized }: Props) {
  const [exceptions, setExceptions] = useState<ScheduleException[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [date, setDate] = useState('')
  const [type, setType] = useState<ExceptionType>('BLOCKED')
  const [wholeDay, setWholeDay] = useState(true)
  const [start, setStart] = useState('10:00')
  const [end, setEnd] = useState('18:00')
  const [saving, setSaving] = useState(false)

  function handleError(e: unknown) {
    if (e instanceof UnauthorizedError) onUnauthorized()
    else setError((e as Error).message)
  }

  useEffect(() => {
    fetchExceptions(employeeId)
      .then(setExceptions)
      .catch((e: unknown) => {
        if (e instanceof UnauthorizedError) onUnauthorized()
        else setError((e as Error).message)
      })
      .finally(() => setLoading(false))
  }, [employeeId, onUnauthorized])

  // Extra opening always needs times; only a block can cover the whole day
  const needsTimes = type === 'EXTRA_OPEN' || !wholeDay

  async function add(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    try {
      const created = await createException(employeeId, {
        date,
        type,
        ...(needsTimes ? { startTime: start, endTime: end } : {}),
      })
      setExceptions((current) =>
        [...current, created].sort((a, b) => a.date.localeCompare(b.date) || (a.startTime ?? '').localeCompare(b.startTime ?? '')),
      )
      setDate('')
    } catch (e) {
      handleError(e)
    } finally {
      setSaving(false)
    }
  }

  async function remove(id: number) {
    setError(null)
    try {
      await deleteException(employeeId, id)
      setExceptions((current) => current.filter((e) => e.id !== id))
    } catch (e) {
      handleError(e)
    }
  }

  return (
    <section className="admin-card">
      <h2>Fridager og endringer</h2>
      <p className="admin-hint">Stenge en dag eller et tidsrom (ferie, sykdom), eller åpne ekstra utenom de faste tidene.</p>

      <form className="exception-form" onSubmit={add}>
        <label className="form-field">
          Dato
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} required />
        </label>
        <label className="form-field">
          Type
          <select value={type} onChange={(e) => setType(e.target.value as ExceptionType)}>
            <option value="BLOCKED">Stengt</option>
            <option value="EXTRA_OPEN">Ekstra åpent</option>
          </select>
        </label>
        {type === 'BLOCKED' && (
          <label className="check-field">
            <input type="checkbox" checked={wholeDay} onChange={(e) => setWholeDay(e.target.checked)} /> Hele dagen
          </label>
        )}
        {needsTimes && (
          <div className="hours-interval">
            <input type="time" aria-label="Fra" value={start} onChange={(e) => setStart(e.target.value)} required />
            <span>–</span>
            <input type="time" aria-label="Til" value={end} onChange={(e) => setEnd(e.target.value)} required />
          </div>
        )}
        <button type="submit" className="button admin-button" disabled={saving}>
          {saving ? 'Legger til…' : 'Legg til'}
        </button>
      </form>

      {error && <p className="error">{error}</p>}
      {loading && <p className="status">Laster…</p>}
      {!loading && exceptions.length === 0 && <p className="admin-muted">Ingen kommende endringer.</p>}

      <ul className="admin-list">
        {exceptions.map((e) => (
          <li key={e.id}>
            <span>
              <strong>{DATE_FORMAT.format(new Date(`${e.date}T00:00:00`))}</strong> · {describe(e)}
            </span>
            <button type="button" className="link-button" onClick={() => remove(e.id)}>
              Fjern
            </button>
          </li>
        ))}
      </ul>
    </section>
  )
}
