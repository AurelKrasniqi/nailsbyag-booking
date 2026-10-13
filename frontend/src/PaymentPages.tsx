import { useEffect, useState } from 'react'
import { ApiError, checkPayment, fakeVippsDecide, type Appointment } from './api/appointments'
import { shortTime } from './dates'
import './PaymentPages.css'

const DAY_FORMAT = new Intl.DateTimeFormat('nb-NO', { weekday: 'long', day: 'numeric', month: 'long' })

// How often and for how long we ask the backend while waiting for the payment to settle
const POLL_INTERVAL_MS = 2000
const MAX_ATTEMPTS = 30

// Where the customer lands after the payment page (/#/betaling/<token>).
// The address alone proves nothing: the backend asks the payment provider what really happened.
export function PaymentReturnPage({ token }: { token: string }) {
  const [appointment, setAppointment] = useState<Appointment | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [gaveUp, setGaveUp] = useState(false)

  useEffect(() => {
    let cancelled = false
    let timer: number | undefined

    async function poll(attempt: number) {
      try {
        const result = await checkPayment(token)
        if (cancelled) return
        setAppointment(result)
        if (result.status === 'PENDING_PAYMENT') {
          if (attempt >= MAX_ATTEMPTS) setGaveUp(true)
          else timer = window.setTimeout(() => poll(attempt + 1), POLL_INTERVAL_MS)
        }
      } catch (e) {
        if (cancelled) return
        if (e instanceof ApiError && e.status === 404) setError('Fant ikke denne bookingen.')
        else setError('Kunne ikke sjekke betalingen akkurat nå. Last siden på nytt om litt.')
      }
    }

    poll(1)
    return () => {
      cancelled = true
      window.clearTimeout(timer)
    }
  }, [token])

  return (
    <main className="payment-page">
      <div className="payment-card">
        {error && <p className="error">{error}</p>}

        {!error && !appointment && <p className="status">Sjekker betalingen…</p>}

        {appointment?.status === 'PENDING_PAYMENT' && !gaveUp && (
          <>
            <h1>Venter på Vipps…</h1>
            <p className="status">Vi bekrefter betalingen din. Dette tar vanligvis bare noen sekunder.</p>
          </>
        )}

        {appointment?.status === 'PENDING_PAYMENT' && gaveUp && (
          <>
            <h1>Vi har ikke fått bekreftelse ennå</h1>
            <p>
              Hvis du fullførte betalingen i Vipps, blir timen bekreftet i løpet av kort tid. Last siden på nytt for å
              sjekke igjen.
            </p>
          </>
        )}

        {appointment?.status === 'BOOKED' && (
          <>
            <h1>Takk, timen er booket!</h1>
            <p>
              <strong>
                {DAY_FORMAT.format(new Date(`${appointment.startTime.slice(0, 10)}T00:00:00`))} kl.{' '}
                {shortTime(appointment.startTime.slice(11))}
              </strong>
            </p>
            <p>
              {appointment.serviceNames.join(' + ')}
              {appointment.addOns.length > 0 && (
                <>
                  <br />
                  Tillegg: {appointment.addOns.map((a) => (a.quantity > 1 ? `${a.name} ×${a.quantity}` : a.name)).join(', ')}
                </>
              )}
              <br />
              {appointment.paymentStatus === 'PAID'
                ? `Betalt med Vipps: ${appointment.totalPriceNok} kr`
                : `Totalt ${appointment.totalPriceNok} kr, betales i salongen`}
            </p>
            <p className="status">Husk å ta et skjermbilde av denne bekreftelsen.</p>
          </>
        )}

        {appointment?.status === 'CANCELLED' && (
          <>
            <h1>Betalingen ble ikke fullført</h1>
            <p>Ingen penger er trukket, og tiden er frigitt. Du kan prøve å booke på nytt.</p>
          </>
        )}

        <a className="button payment-button" href="#/">
          Til forsiden
        </a>
      </div>
    </main>
  )
}

// DEVELOPMENT ONLY: stands in for the Vipps payment page when the backend uses the fake payment provider
// (/#/fake-vipps/<token>). With the real Vipps the customer is sent to Vipps instead and never sees this page.
export function FakeVippsPage({ token }: { token: string }) {
  const [error, setError] = useState<string | null>(null)

  async function decide(decision: 'approve' | 'reject') {
    setError(null)
    try {
      await fakeVippsDecide(token, decision)
      window.location.hash = `#/betaling/${token}`
    } catch {
      setError('Test-Vipps er ikke tilgjengelig (kun i utvikling).')
    }
  }

  return (
    <main className="payment-page">
      <div className="payment-card">
        <h1>Test-Vipps</h1>
        <p className="status">
          Dette er en testside som later som den er Vipps. Den finnes bare i utvikling, uten ekte penger.
        </p>
        {error && <p className="error">{error}</p>}
        <div className="payment-actions">
          <button type="button" className="button payment-button" onClick={() => decide('approve')}>
            Godkjenn betaling
          </button>
          <button type="button" className="link-button" onClick={() => decide('reject')}>
            Avbryt
          </button>
        </div>
      </div>
    </main>
  )
}
