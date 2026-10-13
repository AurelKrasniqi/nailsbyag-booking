import { useEffect, useState } from 'react'
import { cancelAppointment, fetchAppointments, UnauthorizedError, type AdminAppointment } from '../../api/admin'
import { shortTime } from '../../dates'

const DATE_FORMAT = new Intl.DateTimeFormat('nb-NO', { weekday: 'short', day: 'numeric', month: 'long' })

export function UpcomingAppointments({ onUnauthorized }: { onUnauthorized: () => void }) {
  const [appointments, setAppointments] = useState<AdminAppointment[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  function handleError(e: unknown) {
    if (e instanceof UnauthorizedError) onUnauthorized()
    else setError((e as Error).message)
  }

  useEffect(() => {
    fetchAppointments()
      .then((all) => {
        // Local "now" as YYYY-MM-DDTHH:mm so it compares as text with the backend's local date-times
        const now = new Date()
        const pad = (n: number) => String(n).padStart(2, '0')
        const nowText = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}`
        setAppointments(all.filter((a) => a.status === 'BOOKED' && a.endTime >= nowText))
      })
      .catch((e: unknown) => {
        if (e instanceof UnauthorizedError) onUnauthorized()
        else setError((e as Error).message)
      })
      .finally(() => setLoading(false))
  }, [onUnauthorized])

  async function cancel(appointment: AdminAppointment) {
    const refundNote = appointment.paymentStatus === 'PAID' ? ' Kunden har betalt med Vipps og får pengene tilbake.' : ''
    if (!window.confirm(`Avbestille timen til ${appointment.customerName}?${refundNote}`)) return
    setError(null)
    try {
      await cancelAppointment(appointment.id)
      setAppointments((current) => current.filter((a) => a.id !== appointment.id))
    } catch (e) {
      handleError(e)
    }
  }

  return (
    <section className="admin-card">
      <h2>Kommende timer</h2>
      {error && <p className="error">{error}</p>}
      {loading && <p className="status">Laster…</p>}
      {!loading && appointments.length === 0 && <p className="admin-muted">Ingen kommende timer.</p>}

      <ul className="admin-list">
        {appointments.map((a) => (
          <li key={a.id}>
            <span>
              <strong>
                {DATE_FORMAT.format(new Date(`${a.startTime.slice(0, 10)}T00:00:00`))} kl.{' '}
                {shortTime(a.startTime.slice(11))}–{shortTime(a.endTime.slice(11))}
              </strong>
              <br />
              {a.customerName} · {a.customerPhone}
              <br />
              <span className="admin-muted">
                {a.serviceNames.join(' + ')} · {a.totalPriceNok} kr
              </span>
              {a.addOns.length > 0 && (
                <>
                  <br />
                  <span className="admin-muted">
                    Tillegg: {a.addOns.map((x) => (x.quantity > 1 ? `${x.name} ×${x.quantity}` : x.name)).join(', ')}
                  </span>
                </>
              )}
              <br />
              <span className={a.paymentStatus === 'PAID' ? 'admin-success' : 'admin-muted'}>
                {a.paymentStatus === 'PAID' ? '✓ Betalt med Vipps' : 'Betaler i salongen'}
              </span>
              {a.comment && (
                <>
                  <br />
                  <span className="appointment-comment">«{a.comment}»</span>
                </>
              )}
            </span>
            <button type="button" className="link-button" onClick={() => cancel(a)}>
              Avbestill
            </button>
          </li>
        ))}
      </ul>
    </section>
  )
}
