import { useEffect, useState, type FormEvent } from 'react'
import type { AddOn } from '../api/addOns'
import { ApiError, createAppointment, type Appointment, type PaymentMethod } from '../api/appointments'
import { fetchEmployees } from '../api/availability'
import { fetchPaymentOptions } from '../api/paymentOptions'
import type { SalonService } from '../api/salonServices'
import { chosenAddOns, formatChoice, formatDuration, totalDurationMinutes, totalPriceNok } from '../cart'
import { shortTime } from '../dates'
import { AddOnPicker } from './AddOnPicker'
import { BookingCalendar, type Selection } from './BookingCalendar'

const LONG_DAY_FORMAT = new Intl.DateTimeFormat('nb-NO', { weekday: 'long', day: 'numeric', month: 'long' })

// extras = optional step where the customer adds things like glitter or charms (only if some fit the treatment)
type Step = 'extras' | 'pick' | 'details' | 'done'

interface BookingDialogProps {
  items: SalonService[]
  // Extras that fit the chosen treatments, and how many of each is picked (by extra id)
  extras: AddOn[]
  quantities: Record<number, number>
  onQuantityChange: (addOnId: number, quantity: number) => void
  // Close without booking
  onClose: () => void
  // Close after a successful booking (the cart is emptied)
  onBooked: () => void
}

function formatDay(date: string): string {
  return LONG_DAY_FORMAT.format(new Date(`${date}T00:00:00`))
}

export function BookingDialog({ items, extras, quantities, onQuantityChange, onClose, onBooked }: BookingDialogProps) {
  const [step, setStep] = useState<Step>(extras.length > 0 ? 'extras' : 'pick')
  const [employeeId, setEmployeeId] = useState<number | null>(null)
  const [employeeError, setEmployeeError] = useState(false)
  const [selection, setSelection] = useState<Selection | null>(null)
  const [reloadToken, setReloadToken] = useState(0)
  const [name, setName] = useState('')
  const [phone, setPhone] = useState('')
  const [email, setEmail] = useState('')
  const [comment, setComment] = useState('')
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('AT_SALON')
  // Online payment is only offered when the backend has it switched on. Until we know, only paying at the salon.
  const [vippsEnabled, setVippsEnabled] = useState(false)
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

  useEffect(() => {
    fetchPaymentOptions()
      .then((options) => setVippsEnabled(options.vippsEnabled))
      .catch(() => setVippsEnabled(false))
  }, [])

  const serviceIds = items.map((s) => s.id)
  const choices = chosenAddOns(extras, quantities)
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
      const result = await createAppointment({
        employeeId,
        serviceIds,
        addOns: choices.map((c) => ({ addOnId: c.addOn.id, quantity: c.quantity })),
        startTime: `${chosen.date}T${chosen.time}`,
        customer: { name: name.trim(), phone: phone.trim(), email: email.trim() || undefined },
        comment: comment.trim() || undefined,
        paymentMethod: vippsEnabled ? paymentMethod : 'AT_SALON',
      })
      if (result.paymentRedirectUrl) {
        // Pay online: the time is held for a few minutes while the customer pays. Leave for the payment page;
        // it sends the customer back to /#/betaling/<token> when done.
        window.location.assign(result.paymentRedirectUrl)
        return
      }
      setConfirmation(result.appointment)
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

  const title = { extras: 'Vil du legge til noe ekstra?', pick: 'Velg dag og tid', details: 'Dine opplysninger', done: '' }[step]

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
            <h2>{title}</h2>
            <p className="modal-summary">
              {items.map((s) => s.name).join(' + ')}
              {choices.length > 0 && <> + {choices.map(formatChoice).join(', ')}</>} ·{' '}
              {formatDuration(totalDurationMinutes(items, choices))} · <strong>{totalPriceNok(items, choices)} kr</strong>
            </p>
          </>
        )}

        {step === 'extras' && (
          <>
            <AddOnPicker extras={extras} quantities={quantities} onChange={onQuantityChange} />
            <div className="modal-footer">
              <button type="button" className="button" onClick={() => setStep('pick')}>
                {choices.length > 0 ? 'Fortsett' : 'Hopp over'}
              </button>
            </div>
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
                addOns={choices}
                selection={selection}
                onSelect={setSelection}
                reloadToken={reloadToken}
              />
            )}
            <div className="modal-footer modal-footer-split">
              {extras.length > 0 ? (
                <button type="button" className="link-button" onClick={() => setStep('extras')}>
                  ← Endre tillegg
                </button>
              ) : (
                <span />
              )}
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
            <label className="form-field">
              Kommentar til Agneta (valgfritt)
              <textarea
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                maxLength={500}
                rows={3}
                placeholder="For eksempel ønsket farge, inspirasjon eller allergier"
              />
              <span className="char-count">{comment.length}/500</span>
            </label>

            {!vippsEnabled && <p className="pay-at-salon-note">Du betaler i salongen når du kommer til timen.</p>}

            {vippsEnabled && (
            <fieldset className="payment-choice">
              <legend>Betaling</legend>
              <label className={`payment-option ${paymentMethod === 'AT_SALON' ? 'selected' : ''}`}>
                <input
                  type="radio"
                  name="payment"
                  checked={paymentMethod === 'AT_SALON'}
                  onChange={() => setPaymentMethod('AT_SALON')}
                />
                <span>
                  <strong>Betal i salongen</strong>
                  <small>Du betaler når du kommer til timen.</small>
                </span>
              </label>
              <label className={`payment-option ${paymentMethod === 'VIPPS' ? 'selected' : ''}`}>
                <input
                  type="radio"
                  name="payment"
                  checked={paymentMethod === 'VIPPS'}
                  onChange={() => setPaymentMethod('VIPPS')}
                />
                <span>
                  <strong>Betal nå med Vipps ({totalPriceNok(items, choices)} kr)</strong>
                  <small>Du sendes til Vipps. Tiden holdes av i 10 minutter mens du betaler.</small>
                </span>
              </label>
            </fieldset>
            )}

            {submitError && <p className="error">{submitError}</p>}

            <div className="modal-footer">
              <button type="submit" className="button" disabled={submitting}>
                {submitting
                  ? paymentMethod === 'VIPPS' ? 'Sender deg til Vipps…' : 'Booker…'
                  : paymentMethod === 'VIPPS' ? 'Gå til betaling' : 'Bekreft booking'}
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
              {confirmation.addOns.length > 0 && (
                <>
                  <br />
                  Tillegg: {confirmation.addOns.map((a) => (a.quantity > 1 ? `${a.name} ×${a.quantity}` : a.name)).join(', ')}
                </>
              )}
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
