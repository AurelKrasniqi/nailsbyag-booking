import { useState } from 'react'
import type { AddOn } from '../api/addOns'
import type { SalonService } from '../api/salonServices'
import { chosenAddOns, formatChoice, formatDuration, totalDurationMinutes, totalPriceNok } from '../cart'
import { BookingDialog } from './BookingDialog'

interface CartBarProps {
  items: SalonService[]
  // Extras that fit the chosen treatments, and how many of each the customer has picked (by extra id)
  extras: AddOn[]
  quantities: Record<number, number>
  onQuantityChange: (addOnId: number, quantity: number) => void
  onRemove: (id: number) => void
  onClear: () => void
}

// Sticky bar at the bottom of the screen: summary when closed, list of chosen treatments and extras when opened.
export function CartBar({ items, extras, quantities, onQuantityChange, onRemove, onClear }: CartBarProps) {
  const [open, setOpen] = useState(false)
  const [booking, setBooking] = useState(false)

  if (items.length === 0) return null

  const choices = chosenAddOns(extras, quantities)

  return (
    <aside className="cart-bar" aria-label="Valgte behandlinger">
      {open && (
        <div className="cart-panel">
          <ul className="cart-list">
            {items.map((s) => (
              <li key={s.id} className="cart-item">
                <span className="cart-item-name">
                  {s.category && <small>{[s.category, s.subcategory].filter(Boolean).join(' · ')}</small>}
                  {s.name}
                </span>
                <span className="option-duration">{formatDuration(s.durationMinutes)}</span>
                <strong>{s.priceNok} kr</strong>
                <button type="button" className="link-button" onClick={() => onRemove(s.id)}>
                  Fjern
                </button>
              </li>
            ))}
            {choices.map((c) => (
              <li key={`addon-${c.addOn.id}`} className="cart-item">
                <span className="cart-item-name">
                  <small>Tillegg</small>
                  {formatChoice(c)}
                </span>
                <span className="option-duration">{formatDuration(c.addOn.durationMinutes * c.quantity)}</span>
                <strong>{c.addOn.priceNok * c.quantity} kr</strong>
                <button type="button" className="link-button" onClick={() => onQuantityChange(c.addOn.id, 0)}>
                  Fjern
                </button>
              </li>
            ))}
          </ul>
          {extras.length > 0 && choices.length === 0 && (
            <p className="cart-hint">Du kan legge til tillegg som glitter, stener og mer når du trykker «Velg tid».</p>
          )}
          <button type="button" className="link-button" onClick={onClear}>
            Tøm kurven
          </button>
        </div>
      )}

      <div className="cart-summary">
        <button
          type="button"
          className="cart-toggle"
          aria-expanded={open}
          onClick={() => setOpen((o) => !o)}
        >
          {items.length} {items.length === 1 ? 'behandling' : 'behandlinger'}
          {choices.length > 0 && ` + ${choices.length} tillegg`} ·{' '}
          {formatDuration(totalDurationMinutes(items, choices))} · <strong>{totalPriceNok(items, choices)} kr</strong>
        </button>
        <button type="button" className="button" onClick={() => setBooking(true)}>
          Velg tid
        </button>
        <button type="button" className="cart-dismiss" aria-label="Fjern valgte behandlinger" onClick={onClear}>
          ×
        </button>
      </div>

      {booking && (
        <BookingDialog
          items={items}
          extras={extras}
          quantities={quantities}
          onQuantityChange={onQuantityChange}
          onClose={() => setBooking(false)}
          onBooked={() => {
            setBooking(false)
            onClear()
          }}
        />
      )}
    </aside>
  )
}
