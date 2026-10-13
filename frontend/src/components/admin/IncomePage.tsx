import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { UnauthorizedError } from '../../api/admin'
import { fetchIncome, saveSettings, type IncomeReport } from '../../api/income'
import { emptyMonth, formatKr, monthName, sumMonths, type Period } from '../../income'

interface Row {
  key: string
  label: string
  gross: number
  vat: number
  net: number
  appointments: number
  // What clicking the row selects
  select: () => void
  selected: boolean
}

// Earnings overview, in the style of a host dashboard: what customers paid, minus VAT, is what is actually earned.
// Pick a month, a year or everything; the figures are the sum of the months below.
export function IncomePage({ onUnauthorized }: { onUnauthorized: () => void }) {
  // Fixed when the page opens (not on every render)
  const [today] = useState(() => new Date())
  const [report, setReport] = useState<IncomeReport | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [period, setPeriod] = useState<Period>('month')
  const [year, setYear] = useState(today.getFullYear())
  const [month, setMonth] = useState(today.getMonth() + 1)

  const load = useCallback(() => {
    fetchIncome()
      .then(setReport)
      .catch((e: unknown) => {
        if (e instanceof UnauthorizedError) onUnauthorized()
        else setError((e as Error).message)
      })
  }, [onUnauthorized])

  useEffect(() => {
    load()
  }, [load])

  if (error) return <p className="error">{error}</p>
  if (!report) return <p className="status">Laster inntekt…</p>

  const months = report.months
  const selectedMonths =
    period === 'month'
      ? months.filter((m) => m.year === year && m.month === month)
      : period === 'year'
        ? months.filter((m) => m.year === year)
        : months
  const totals = sumMonths(selectedMonths)

  const isCurrentMonth = year === today.getFullYear() && month === today.getMonth() + 1
  const isCurrentYear = year === today.getFullYear()

  function stepMonth(delta: number) {
    const d = new Date(year, month - 1 + delta, 1)
    setYear(d.getFullYear())
    setMonth(d.getMonth() + 1)
  }

  const periodLabel =
    period === 'month' ? `${monthName(month)} ${year}` : period === 'year' ? String(year) : 'Hele tiden'

  // The table under the totals: the twelve months of the chosen year, or one row per year when showing everything
  const rows: Row[] =
    period === 'all'
      ? [...new Set(months.map((m) => m.year))].map((y) => {
          const t = sumMonths(months.filter((m) => m.year === y))
          return {
            key: String(y),
            label: String(y),
            gross: t.grossOre,
            vat: t.vatOre,
            net: t.netOre,
            appointments: t.appointments,
            select: () => {
              setYear(y)
              setPeriod('year')
            },
            selected: false,
          }
        })
      : Array.from({ length: 12 }, (_, i) => {
          const m = months.find((x) => x.year === year && x.month === i + 1) ?? emptyMonth(year, i + 1)
          return {
            key: `${year}-${i + 1}`,
            label: monthName(i + 1),
            gross: m.grossOre,
            vat: m.vatOre,
            net: m.netOre,
            appointments: m.appointments,
            select: () => {
              setMonth(i + 1)
              setPeriod('month')
            },
            selected: period === 'month' && month === i + 1,
          }
        })
  const maxNet = Math.max(1, ...rows.map((r) => r.net))
  const vatLabel = `MVA (${report.vatRatePercent} %)`

  return (
    <>
      <section className="admin-card">
        <div className="period-bar">
          <div className="segmented" role="tablist" aria-label="Periode">
            {(
              [
                ['month', 'Måned'],
                ['year', 'År'],
                ['all', 'Totalt'],
              ] as const
            ).map(([value, label]) => (
              <button
                key={value}
                type="button"
                role="tab"
                aria-selected={period === value}
                className={period === value ? 'active' : ''}
                onClick={() => setPeriod(value)}
              >
                {label}
              </button>
            ))}
          </div>

          {period !== 'all' && (
            <div className="period-nav">
              <button
                type="button"
                className="link-button"
                aria-label="Forrige"
                onClick={() => (period === 'month' ? stepMonth(-1) : setYear(year - 1))}
              >
                ←
              </button>
              <strong className="period-label">{periodLabel}</strong>
              <button
                type="button"
                className="link-button"
                aria-label="Neste"
                disabled={period === 'month' ? isCurrentMonth : isCurrentYear}
                onClick={() => (period === 'month' ? stepMonth(1) : setYear(year + 1))}
              >
                →
              </button>
            </div>
          )}
          {period === 'all' && <strong className="period-label">{periodLabel}</strong>}
        </div>

        <p className="earnings-label">Faktisk inntjening</p>
        <p className="earnings-amount">{formatKr(totals.netOre)}</p>

        <dl className="breakdown">
          <div>
            <dt>Totalt betalt av kunder{report.vatRegistered ? ' (inkl. mva)' : ''}</dt>
            <dd>{formatKr(totals.grossOre)}</dd>
          </div>
          {report.vatRegistered ? (
            <div>
              <dt>− {vatLabel}</dt>
              <dd>−{formatKr(totals.vatOre)}</dd>
            </div>
          ) : (
            <div>
              <dt>MVA</dt>
              <dd className="admin-muted">Ikke registrert, ingenting trekkes fra</dd>
            </div>
          )}
          <div className="breakdown-total">
            <dt>Faktisk inntjening</dt>
            <dd>{formatKr(totals.netOre)}</dd>
          </div>
        </dl>

        <p className="admin-hint">
          {totals.appointments} {totals.appointments === 1 ? 'time' : 'timer'}
          {totals.appointments > 0 && <> · snitt {formatKr(Math.round(totals.grossOre / totals.appointments))} pr. time</>}
          {report.upcoming.appointments > 0 && (
            <>
              <br />
              Kommende: {report.upcoming.appointments} {report.upcoming.appointments === 1 ? 'time' : 'timer'} på{' '}
              {formatKr(report.upcoming.grossOre)}. Regnes med som inntekt når timene er gjennomført.
            </>
          )}
        </p>
      </section>

      <section className="admin-card">
        <h2>{period === 'all' ? 'Per år' : `Per måned, ${year}`}</h2>
        {rows.length === 0 && <p className="admin-muted">Ingen inntekt ennå.</p>}
        <div className="income-table" role="table">
          <div className="income-row income-head" role="row">
            <span role="columnheader">{period === 'all' ? 'År' : 'Måned'}</span>
            <span role="columnheader">Timer</span>
            <span role="columnheader">Totalt</span>
            {report.vatRegistered && <span role="columnheader">MVA</span>}
            <span role="columnheader">Faktisk inntjening</span>
          </div>
          {rows.map((r) => (
            <button
              key={r.key}
              type="button"
              role="row"
              className={`income-row income-line ${r.selected ? 'selected' : ''}`}
              onClick={r.select}
            >
              <span className="income-label">{r.label}</span>
              <span>{r.appointments}</span>
              <span>{formatKr(r.gross)}</span>
              {report.vatRegistered && <span>{formatKr(r.vat)}</span>}
              <span className="income-net">
                {formatKr(r.net)}
                <i className="income-bar" style={{ width: `${(r.net / maxNet) * 100}%` }} />
              </span>
            </button>
          ))}
        </div>
      </section>

      <VatSettings
        report={report}
        onSaved={load}
        onUnauthorized={onUnauthorized}
      />
    </>
  )
}

function VatSettings({
  report,
  onSaved,
  onUnauthorized,
}: {
  report: IncomeReport
  onSaved: () => void
  onUnauthorized: () => void
}) {
  const [registered, setRegistered] = useState(report.vatRegistered)
  const [rate, setRate] = useState(String(report.vatRatePercent))
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState<{ text: string; error: boolean } | null>(null)

  async function save(event: FormEvent) {
    event.preventDefault()
    const parsed = Number(rate)
    if (!Number.isInteger(parsed) || parsed < 0 || parsed > 100) {
      setMessage({ text: 'Satsen må være et helt tall mellom 0 og 100.', error: true })
      return
    }
    setSaving(true)
    setMessage(null)
    try {
      await saveSettings({ vatRegistered: registered, vatRatePercent: parsed })
      setMessage({ text: 'Lagret. Tallene over er oppdatert.', error: false })
      onSaved()
    } catch (e) {
      if (e instanceof UnauthorizedError) onUnauthorized()
      else setMessage({ text: (e as Error).message, error: true })
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="admin-card">
      <h2>MVA</h2>
      <p className="admin-hint">
        Prisene på nettsiden er oppgitt inkludert mva. Er du MVA-registrert, regnes mva ut av hver måneds beløp og
        trekkes fra, slik at du ser hva du faktisk sitter igjen med. Er du ikke registrert, trekkes ingenting.
        Spør regnskapsfører eller Skatteetaten om du skal være registrert.
      </p>
      <form className="vat-form" onSubmit={save}>
        <label className="check-field">
          <input type="checkbox" checked={registered} onChange={(e) => setRegistered(e.target.checked)} /> Jeg er
          MVA-registrert
        </label>
        <label className="form-field">
          Sats (%)
          <input
            type="number"
            min={0}
            max={100}
            step={1}
            value={rate}
            disabled={!registered}
            onChange={(e) => setRate(e.target.value)}
          />
        </label>
        <button type="submit" className="button admin-button" disabled={saving}>
          {saving ? 'Lagrer…' : 'Lagre'}
        </button>
      </form>
      {message && <p className={message.error ? 'error' : 'admin-success'}>{message.text}</p>}
    </section>
  )
}
