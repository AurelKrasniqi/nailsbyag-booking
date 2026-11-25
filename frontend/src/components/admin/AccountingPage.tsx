import { useCallback, useEffect, useState } from 'react'
import { fetchAccountingPdf, UnauthorizedError } from '../../api/admin'
import { fetchIncome, type IncomeReport } from '../../api/income'
import { monthRange, toDateString } from '../../dates'
import { formatKr, monthName, sumMonths } from '../../income'

type Mode = 'month' | 'year' | 'custom'

// "Regnskap": pick a month, a year or your own dates and download a PDF to hand to the accountant.
// The PDF is made by the backend (same rules as the income page). This page picks the period and previews the totals.
export function AccountingPage({ onUnauthorized }: { onUnauthorized: () => void }) {
  const [today] = useState(() => new Date())
  const [mode, setMode] = useState<Mode>('month')
  const [year, setYear] = useState(today.getFullYear())
  const [month, setMonth] = useState(today.getMonth() + 1)
  const [customFrom, setCustomFrom] = useState(() => monthRange(new Date(today.getFullYear(), today.getMonth(), 1)).from)
  const [customTo, setCustomTo] = useState(() => toDateString(today))
  const [newestFirst, setNewestFirst] = useState(false)

  const [report, setReport] = useState<IncomeReport | null>(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleError = useCallback(
    (e: unknown) => {
      if (e instanceof UnauthorizedError) onUnauthorized()
      else setError((e as Error).message)
    },
    [onUnauthorized],
  )

  // The totals shown before downloading come from the same figures as the income page
  useEffect(() => {
    fetchIncome().then(setReport).catch(handleError)
  }, [handleError])

  const { from, to } =
    mode === 'month'
      ? monthRange(new Date(year, month - 1, 1))
      : mode === 'year'
        ? { from: `${year}-01-01`, to: `${year}-12-31` }
        : { from: customFrom, to: customTo }

  const valid = from !== '' && to !== '' && from <= to
  const isCurrentMonth = year === today.getFullYear() && month === today.getMonth() + 1
  const isCurrentYear = year === today.getFullYear()

  function stepMonth(delta: number) {
    const d = new Date(year, month - 1 + delta, 1)
    setYear(d.getFullYear())
    setMonth(d.getMonth() + 1)
  }

  const periodLabel = mode === 'month' ? `${monthName(month)} ${year}` : String(year)

  // Only a month or a year can be previewed: they are exactly the sums of the monthly figures
  const totals =
    report && mode !== 'custom'
      ? sumMonths(report.months.filter((m) => m.year === year && (mode === 'year' || m.month === month)))
      : null

  async function download() {
    setBusy(true)
    setError(null)
    try {
      const blob = await fetchAccountingPdf(from, to, newestFirst)
      // A temporary address for the file, clicked once so the browser saves it
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `regnskap-${from}-til-${to}.pdf`
      link.click()
      URL.revokeObjectURL(url)
    } catch (e) {
      handleError(e)
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <section className="admin-card">
        <h2>Velg periode</h2>
        <div className="period-bar">
          <div className="segmented" role="tablist" aria-label="Periode">
            {(
              [
                ['month', 'Måned'],
                ['year', 'År'],
                ['custom', 'Egen periode'],
              ] as const
            ).map(([value, label]) => (
              <button
                key={value}
                type="button"
                role="tab"
                aria-selected={mode === value}
                className={mode === value ? 'active' : ''}
                onClick={() => setMode(value)}
              >
                {label}
              </button>
            ))}
          </div>

          {mode !== 'custom' && (
            <div className="period-nav">
              <button
                type="button"
                className="link-button"
                aria-label="Forrige"
                onClick={() => (mode === 'month' ? stepMonth(-1) : setYear(year - 1))}
              >
                ←
              </button>
              <strong className="period-label">{periodLabel}</strong>
              <button
                type="button"
                className="link-button"
                aria-label="Neste"
                disabled={mode === 'month' ? isCurrentMonth : isCurrentYear}
                onClick={() => (mode === 'month' ? stepMonth(1) : setYear(year + 1))}
              >
                →
              </button>
            </div>
          )}
        </div>

        {mode === 'custom' && (
          <div className="accounting-dates">
            <div className="editor-field">
              <label htmlFor="acc-from">Fra dato</label>
              <input id="acc-from" type="date" value={customFrom} onChange={(e) => setCustomFrom(e.target.value)} />
            </div>
            <div className="editor-field">
              <label htmlFor="acc-to">Til dato (til og med)</label>
              <input id="acc-to" type="date" value={customTo} onChange={(e) => setCustomTo(e.target.value)} />
            </div>
            <p className="field-hint">Høyst ett år om gangen.</p>
          </div>
        )}
      </section>

      <section className="admin-card">
        <h2>Innhold i PDF-en</h2>

        {totals ? (
          <dl className="breakdown">
            <div>
              <dt>Antall timer</dt>
              <dd>{totals.appointments}</dd>
            </div>
            <div>
              <dt>Totalt betalt av kunder{report?.vatRegistered ? ' (inkl. mva)' : ''}</dt>
              <dd>{formatKr(totals.grossOre)}</dd>
            </div>
            {report?.vatRegistered && (
              <div>
                <dt>MVA ({report.vatRatePercent} %)</dt>
                <dd>{formatKr(totals.vatOre)}</dd>
              </div>
            )}
            <div className="breakdown-total">
              <dt>Faktisk inntjening</dt>
              <dd>{formatKr(totals.netOre)}</dd>
            </div>
          </dl>
        ) : (
          <p className="admin-hint">
            {mode === 'custom'
              ? 'Sammendraget med sum, MVA og inntekt står øverst i PDF-en.'
              : 'Henter tall…'}
          </p>
        )}

        <p className="admin-hint">
          PDF-en viser sammendrag, tall per måned og en liste med alle gjennomførte timer. Kommende og avbestilte timer
          er ikke med. Vipps-gebyret (2,99 % + 1 kr per betaling) står som et estimert fradrag i PDF-en, og er ikke
          trukket fra tallene over.
        </p>

        <div className="accounting-actions">
          <div className="accounting-sort">
            <span className="admin-muted">Sortering</span>
            <div className="segmented" role="tablist" aria-label="Sortering">
              {(
                [
                  [false, 'Eldste først'],
                  [true, 'Nyeste først'],
                ] as const
              ).map(([value, label]) => (
                <button
                  key={label}
                  type="button"
                  role="tab"
                  aria-selected={newestFirst === value}
                  className={newestFirst === value ? 'active' : ''}
                  onClick={() => setNewestFirst(value)}
                >
                  {label}
                </button>
              ))}
            </div>
          </div>

          <button type="button" className="button admin-button" disabled={!valid || busy} onClick={download}>
            {busy ? 'Lager PDF…' : 'Last ned PDF'}
          </button>
        </div>

        {error && <p className="error">{error}</p>}
        {!valid && <p className="error">Fra-datoen må være før eller lik til-datoen.</p>}
      </section>
    </>
  )
}
