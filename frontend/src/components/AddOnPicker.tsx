import type { AddOn } from '../api/addOns'
import { formatAddOnPrice } from '../cart'

interface AddOnPickerProps {
  extras: AddOn[]
  quantities: Record<number, number>
  onChange: (addOnId: number, quantity: number) => void
}

// The extras ("Tillegg") that fit the chosen treatment. An extra that is priced per nail gets a - and + to pick how
// many nails (up to ten); a one-off extra is a simple toggle.
export function AddOnPicker({ extras, quantities, onChange }: AddOnPickerProps) {
  return (
    <ul className="addon-list">
      {extras.map((extra) => {
        const quantity = quantities[extra.id] ?? 0
        return (
          <li key={extra.id} className="addon-row">
            <span className="addon-name">
              <strong>{extra.name}</strong>
              <small>{formatAddOnPrice(extra)}</small>
            </span>

            {extra.maxQuantity === 1 ? (
              <button
                type="button"
                className={`add-button ${quantity > 0 ? 'selected' : ''}`}
                aria-pressed={quantity > 0}
                onClick={() => onChange(extra.id, quantity > 0 ? 0 : 1)}
              >
                {quantity > 0 ? '✓ Valgt' : 'Legg til'}
              </button>
            ) : (
              <span className="stepper" role="group" aria-label={`Antall: ${extra.name}`}>
                <button
                  type="button"
                  className="stepper-button"
                  aria-label={`Færre: ${extra.name}`}
                  disabled={quantity === 0}
                  onClick={() => onChange(extra.id, quantity - 1)}
                >
                  −
                </button>
                <output className="stepper-value" aria-live="polite">
                  {quantity}
                </output>
                <button
                  type="button"
                  className="stepper-button"
                  aria-label={`Flere: ${extra.name}`}
                  disabled={quantity >= extra.maxQuantity}
                  onClick={() => onChange(extra.id, quantity + 1)}
                >
                  +
                </button>
              </span>
            )}
          </li>
        )
      })}
    </ul>
  )
}
