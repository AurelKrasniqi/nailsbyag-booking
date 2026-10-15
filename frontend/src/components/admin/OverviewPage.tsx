import { useEffect, useState } from 'react'
import { fetchAppointments, UnauthorizedError, type AdminAppointment } from '../../api/admin'
import { fetchIncome, type IncomeReport } from '../../api/income'
import { shortTime } from '../../dates'
import { formatKr, monthName, sumMonths } from '../../income'

const DATE_FORMAT = new Intl.DateTimeFormat('nb-NO', { weekday: 'long', day: 'numeric', month: 'long' })

// The front page of the dashboard: a few numbers, each linking to the page that has the details
export function OverviewPage({ onUnauthorized }: { onUnauthorized: () => void }) {
  const [appointments, setAppointments] = useState<AdminAppointment[] | null>(null)
  const [income, setIncome] = useState<IncomeReport | null>(null)
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
  }, [onUnauthorized])

  if (error) return <p className="error">{error}</p>
  if (!appointments || !income) return <p className="status">Laster…</p>

  // Local "now" as text so it compares with the backend's local date-times
  const pad = (n: number) => String(n).padStart(2, '0')
  const nowText = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}`
  const upcoming = appointments
    .filter((a) => a.status === 'BOOKED' && a.endTime >= nowText)
    .sort((a, b) => a.startTime.localeCompare(b.startTime))
  const next = upcoming[0]

  const thisMonth = sumMonths(
    income.months.filter((m) => m.year === now.getFullYear() && m.month === now.getMonth() + 1),
  )
  const thisYear = sumMonths(income.months.filter((m) => m.year === now.getFullYear()))

  return (
    <div className="stat-grid">
      <section className="admin-card stat-card">
        <p className="stat-label">Neste time</p>
        {next ? (
          <>
            <p className="stat-value stat-value-small">
              {DATE_FORMAT.format(new Date(`${next.startTime.slice(0, 10)}T00:00:00`))} kl. {shortTime(next.startTime.slice(11))}
            </p>
            <p className="stat-hint">
              {next.customerName} · {next.serviceNames.join(' + ')}
            </p>
          </>
        ) : (
          <p className="stat-hint">Ingen kommende timer.</p>
        )}
        <a href="#/admin/timer">Se alle timer →</a>
      </section>

      <section className="admin-card stat-card">
        <p className="stat-label">Kommende timer</p>
        <p className="stat-value">{upcoming.length}</p>
        <p className="stat-hint">Booket og ikke gjennomført</p>
        <a href="#/admin/timer">Se timene →</a>
      </section>

      <section className="admin-card stat-card">
        <p className="stat-label">Faktisk inntjening, {monthName(now.getMonth() + 1)}</p>
        <p className="stat-value">{formatKr(thisMonth.netOre)}</p>
        <p className="stat-hint">
          {thisMonth.appointments} {thisMonth.appointments === 1 ? 'time' : 'timer'}
          {income.vatRegistered ? ', etter mva' : ''}
        </p>
        <a href="#/admin/inntekt">Se inntekt →</a>
      </section>

      <section className="admin-card stat-card">
        <p className="stat-label">Faktisk inntjening, {now.getFullYear()}</p>
        <p className="stat-value">{formatKr(thisYear.netOre)}</p>
        <p className="stat-hint">
          {thisYear.appointments} {thisYear.appointments === 1 ? 'time' : 'timer'} hittil i år
        </p>
        <a href="#/admin/inntekt">Se inntekt →</a>
      </section>

      <section className="admin-card stat-card">
        <p className="stat-label">Om oss</p>
        <p className="stat-value stat-value-small">Tekst og bilde på forsiden</p>
        <p className="stat-hint">Endre teksten og bildet i «Om oss»-boksen</p>
        <a href="#/admin/om-oss">Rediger →</a>
      </section>
    </div>
  )
}
