import { useState } from 'react'
import type { SalonService } from '../api/salonServices'
import { BookingDialog } from './BookingDialog'
import { formatDuration, totalDurationMinutes, totalPriceNok } from '../cart'

interface CartBarProps {
  items: SalonService[]
  onRemove: (id: number) => void
  onClear: () => void
}

// Sticky bar at the bottom of the screen: summary when closed, list of chosen treatments when opened.
export function CartBar({ items, onRemove, onClear }: CartBarProps) {
  const [open, setOpen] = useState(false)
  const [booking, setBooking] = useState(false)

  if (items.length === 0) return null

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
          </ul>
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
          {items.length} {items.length === 1 ? 'behandling' : 'behandlinger'} · {formatDuration(totalDurationMinutes(items))} ·{' '}
          <strong>{totalPriceNok(items)} kr</strong>
        </button>
        <button type="button" className="button" onClick={() => setBooking(true)}>
          Velg tid
        </button>
      </div>

      {booking && (
        <BookingDialog
          items={items}
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
