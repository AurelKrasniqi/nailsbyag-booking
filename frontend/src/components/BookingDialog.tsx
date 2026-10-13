import { useEffect, useState, type FormEvent } from 'react'
import { ApiError, createAppointment, type Appointment } from '../api/appointments'
import { fetchEmployees } from '../api/availability'
import type { SalonService } from '../api/salonServices'
import { formatDuration, totalDurationMinutes, totalPriceNok } from '../cart'
import { shortTime } from '../dates'
import { BookingCalendar, type Selection } from './BookingCalendar'

const LONG_DAY_FORMAT = new Intl.DateTimeFormat('nb-NO', { weekday: 'long', day: 'numeric', month: 'long' })

type Step = 'pick' | 'details' | 'done'

interface BookingDialogProps {
  items: SalonService[]
  // Close without booking
  onClose: () => void
  // Close after a successful booking (the cart is emptied)
  onBooked: () => void
}

function formatDay(date: string): string {
  return LONG_DAY_FORMAT.format(new Date(`${date}T00:00:00`))
}

export function BookingDialog({ items, onClose, onBooked }: BookingDialogProps) {
  const [step, setStep] = useState<Step>('pick')
  const [employeeId, setEmployeeId] = useState<number | null>(null)
  const [employeeError, setEmployeeError] = useState(false)
  const [selection, setSelection] = useState<Selection | null>(null)
  const [reloadToken, setReloadToken] = useState(0)
  const [name, setName] = useState('')
  const [phone, setPhone] = useState('')
  const [email, setEmail] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)
  const [confirmation, setConfirmation] = useState<Appointment | null>(null)

  // AG works alone, so the first employee is the one customers book with
  useEffect(() => {
    fetchEmployees()
      .then((employees) => {
        if (employees.length === 0) setEmployeeError(true)
        else setEmployeeId(employees[0].id)
      })
      .catch(() => setEmployeeError(true))
  }, [])

  const serviceIds = items.map((s) => s.id)
  const chosen = selection && selection.time ? { date: selection.date, time: selection.time } : null

  function close() {
    if (step === 'done') onBooked()
    else onClose()
  }

  async function submit(event: FormEvent) {
    event.preventDefault()
    if (!chosen || employeeId === null) return
    setSubmitting(true)
    setSubmitError(null)
    try {
      const appointment = await createAppointment({
        employeeId,
        serviceIds,
        startTime: `${chosen.date}T${chosen.time}`,
        customer: { name: name.trim(), phone: phone.trim(), email: email.trim() || undefined },
      })
      setConfirmation(appointment)
      setStep('done')
    } catch (e) {
      if (e instanceof ApiError && e.status === 409) {
        // Someone else got the time first: back to the calendar with fresh availability
        setSubmitError('Beklager, denne tiden ble akkurat tatt. Velg en annen tid.')
        setSelection(null)
        setReloadToken((t) => t + 1)
        setStep('pick')
      } else {
        setSubmitError('Noe gikk galt under bookingen. Sjekk opplysningene og prøv igjen.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="modal-backdrop" onClick={close}>
      <div
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-label="Book time"
        onClick={(e) => e.stopPropagation()}
      >
        <button type="button" className="modal-close" aria-label="Lukk" onClick={close}>
          ×
        </button>

        {step !== 'done' && (
          <>
            <h2>{step === 'pick' ? 'Velg dag og tid' : 'Dine opplysninger'}</h2>
            <p className="modal-summary">
              {items.map((s) => s.name).join(' + ')} · {formatDuration(totalDurationMinutes(items))} ·{' '}
              <strong>{totalPriceNok(items)} kr</strong>
            </p>
          </>
        )}

        {step === 'pick' && (
          <>
            {submitError && <p className="error">{submitError}</p>}
            {employeeError && <p className="error">Kunne ikke laste kalenderen. Prøv igjen senere.</p>}
            {employeeId !== null && (
              <BookingCalendar
                employeeId={employeeId}
                serviceIds={serviceIds}
                selection={selection}
                onSelect={setSelection}
                reloadToken={reloadToken}
              />
            )}
            <div className="modal-footer">
              <button type="button" className="button" disabled={!chosen} onClick={() => setStep('details')}>
                Fortsett
              </button>
            </div>
          </>
        )}

        {step === 'details' && chosen && (
          <form onSubmit={submit}>
            <p className="chosen-time">
              <strong>
                {formatDay(chosen.date)} kl. {shortTime(chosen.time)}
              </strong>{' '}
              <button type="button" className="link-button" onClick={() => setStep('pick')}>
                Endre
              </button>
            </p>

            <label className="form-field">
              Navn
              <input value={name} onChange={(e) => setName(e.target.value)} required maxLength={100} autoComplete="name" />
            </label>
            <label className="form-field">
              Telefon
              <input
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                required
                maxLength={30}
                type="tel"
                inputMode="tel"
                autoComplete="tel"
              />
            </label>
            <label className="form-field">
              E-post (valgfritt)
              <input
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                maxLength={150}
                type="email"
                autoComplete="email"
              />
            </label>

            {submitError && <p className="error">{submitError}</p>}

            <div className="modal-footer">
              <button type="submit" className="button" disabled={submitting}>
                {submitting ? 'Booker…' : 'Bekreft booking'}
              </button>
            </div>
          </form>
        )}

        {step === 'done' && confirmation && (
          <div className="confirmation">
            <h2>Takk, timen er booket!</h2>
            <p>
              <strong>
                {formatDay(confirmation.startTime.slice(0, 10))} kl. {shortTime(confirmation.startTime.slice(11))}
              </strong>
            </p>
            <p>
              {confirmation.serviceNames.join(' + ')}
              <br />
              Totalt {confirmation.totalPriceNok} kr
            </p>
            <p className="status">Husk å ta et skjermbilde av denne bekreftelsen.</p>
            <div className="modal-footer">
              <button type="button" className="button" onClick={onBooked}>
                Lukk
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
