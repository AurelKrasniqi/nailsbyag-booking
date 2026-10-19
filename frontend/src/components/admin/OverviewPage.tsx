import { useEffect, useState } from 'react'
import {
  fetchAppointments,
  fetchExceptions,
  UnauthorizedError,
  type AdminAppointment,
  type ScheduleException,
} from '../../api/admin'
import { fetchIncome, type IncomeReport } from '../../api/income'
import { shortTime, toDateString } from '../../dates'
import { formatKr, monthName, sumMonths } from '../../income'
import './Overview.css'

const FULL_DATE = new Intl.DateTimeFormat('nb-NO', { weekday: 'long', day: 'numeric', month: 'long' })
const SHORT_DATE = new Intl.DateTimeFormat('nb-NO', { weekday: 'short', day: 'numeric', month: 'short' })

const UPCOMING_SHOWN = 5
const BLOCKS_SHOWN = 4

// Each card on the "Administrer" row: where it goes and what is behind it
const SHORTCUTS = [
  { href: '#/admin/apningstider', title: 'Åpningstider', text: 'Faste åpningstider og ekstra åpent' },
  { href: '#/admin/priser', title: 'Priser', text: 'Behandlinger, priser og tillegg' },
  { href: '#/admin/meldinger', title: 'Meldinger', text: 'SMS-ene kundene får' },
  { href: '#/admin/om-oss', title: 'Om oss', text: 'Tekst og bilde på forsiden' },
]

function greeting(hour: number): string {
  if (hour < 10) return 'God morgen'
  if (hour < 18) return 'God dag'
  return 'God kveld'
}

// "I dag", "I morgen" or "ons. 21. okt."
function dayLabel(date: string, today: string, tomorrow: string): string {
  if (date === today) return 'I dag'
  if (date === tomorrow) return 'I morgen'
  return SHORT_DATE.format(new Date(`${date}T00:00:00`))
}

interface Props {
  employeeId: number
  onUnauthorized: () => void
}

// The front page of the dashboard: what is happening now. Details live on the pages in the menu.
export function OverviewPage({ employeeId, onUnauthorized }: Props) {
  const [appointments, setAppointments] = useState<AdminAppointment[] | null>(null)
  const [income, setIncome] = useState<IncomeReport | null>(null)
  const [exceptions, setExceptions] = useState<ScheduleException[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  // Fixed when the page opens (not on every render)
  const [now] = useState(() => new Date())

  useEffect(() => {
    function fail(e: unknown) {
      if (e instanceof UnauthorizedError) onUnauthorized()
      else setError((e as Error).message)
    }
    fetchAppointments().then(setAppointments).catch(fail)
    fetchIncome().then(setIncome).catch(fail)
    fetchExceptions(employeeId).then(setExceptions).catch(fail)
  }, [employeeId, onUnauthorized])

  if (error) return <p className="error">{error}</p>
  if (!appointments || !income || !exceptions) return <p className="status">Laster…</p>

  const today = toDateString(now)
  const tomorrow = toDateString(new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1))
  // Local "now" as text so it compares with the backend's local date-times
  const pad = (n: number) => String(n).padStart(2, '0')
  const nowText = `${today}T${pad(now.getHours())}:${pad(now.getMinutes())}`

  const upcoming = appointments
    .filter((a) => a.status === 'BOOKED' && a.endTime >= nowText)
    .sort((a, b) => a.startTime.localeCompare(b.startTime))
  const todays = upcoming.filter((a) => a.startTime.startsWith(today))
  const blocks = exceptions.filter((e) => e.type === 'BLOCKED')

  // Only the current month: the full history is on the Inntekt page
  const thisMonth = sumMonths(
    income.months.filter((m) => m.year === now.getFullYear() && m.month === now.getMonth() + 1),
  )

  return (
    <div className="overview">
      <header className="overview-welcome">
        <h2>{greeting(now.getHours())}, Agneta</h2>
        <p className="admin-muted overview-date">{FULL_DATE.format(now)}</p>
      </header>

      <div className="stat-grid">
        <section className="admin-card stat-card">
          <p className="stat-label">I dag</p>
          <p className="stat-value">{todays.length}</p>
          <p className="stat-hint">
            {todays.length === 0
              ? 'Ingen timer igjen i dag'
              : `${todays.length === 1 ? 'time' : 'timer'} igjen · neste kl. ${shortTime(todays[0].startTime.slice(11))}`}
          </p>
          <a href="#/admin/timer">Se alle timer →</a>
        </section>

        <section className="admin-card stat-card">
          <p className="stat-label">Kommende timer</p>
          <p className="stat-value">{upcoming.length}</p>
          <p className="stat-hint">Booket og ikke gjennomført</p>
          <a href="#/admin/kalender">Åpne kalenderen →</a>
        </section>

        <section className="admin-card stat-card">
          <p className="stat-label">Inntjening i {monthName(now.getMonth() + 1)}</p>
          <p className="stat-value">{formatKr(thisMonth.netOre)}</p>
          <p className="stat-hint">
            {thisMonth.appointments} {thisMonth.appointments === 1 ? 'time' : 'timer'}
            {income.vatRegistered ? ' · etter mva' : ''}
          </p>
          <a href="#/admin/inntekt">Se full inntekt →</a>
        </section>
      </div>

      <div className="overview-columns">
        <section className="admin-card">
          <div className="overview-card-head">
            <h2>Neste timer</h2>
            <a href="#/admin/timer">Se alle →</a>
          </div>
          {upcoming.length === 0 && <p className="admin-muted">Ingen kommende timer.</p>}
          <ul className="admin-list overview-list">
            {upcoming.slice(0, UPCOMING_SHOWN).map((a) => (
              <li key={a.id}>
                <span className="overview-when">
                  <strong>{dayLabel(a.startTime.slice(0, 10), today, tomorrow)}</strong>
                  <span>
                    {shortTime(a.startTime.slice(11))}–{shortTime(a.endTime.slice(11))}
                  </span>
                </span>
                <span className="overview-what">
                  {a.customerName}
                  <span className="admin-muted">{a.serviceNames.join(' + ')}</span>
                </span>
                {a.paymentStatus === 'PAID' && <span className="overview-paid">Betalt</span>}
              </li>
            ))}
          </ul>
          {upcoming.length > UPCOMING_SHOWN && (
            <p className="admin-muted overview-more">+ {upcoming.length - UPCOMING_SHOWN} flere</p>
          )}
        </section>

        <section className="admin-card">
          <div className="overview-card-head">
            <h2>Blokkert fremover</h2>
            <a href="#/admin/kalender">Kalender →</a>
          </div>
          {blocks.length === 0 && <p className="admin-muted">Ingen blokkerte tider. Bruk kalenderen hvis noen booker utenom nettsiden.</p>}
          <ul className="admin-list overview-list">
            {blocks.slice(0, BLOCKS_SHOWN).map((b) => (
              <li key={b.id}>
                <span className="overview-when">
                  <strong>{dayLabel(b.date, today, tomorrow)}</strong>
                  <span>{b.startTime && b.endTime ? `${shortTime(b.startTime)}–${shortTime(b.endTime)}` : 'Hele dagen'}</span>
                </span>
              </li>
            ))}
          </ul>
          {blocks.length > BLOCKS_SHOWN && <p className="admin-muted overview-more">+ {blocks.length - BLOCKS_SHOWN} flere</p>}
        </section>
      </div>

      <h2 className="overview-section-title">Administrer</h2>
      <nav className="shortcut-grid" aria-label="Snarveier">
        {SHORTCUTS.map((s) => (
          <a key={s.href} href={s.href} className="shortcut-tile">
            <strong>{s.title}</strong>
            <span>{s.text}</span>
          </a>
        ))}
      </nav>
    </div>
  )
}
