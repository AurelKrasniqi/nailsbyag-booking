import type { SalonService } from '../api/salonServices'
import type { ServiceCardData } from '../groupServices'

interface ServiceCardProps {
  card: ServiceCardData
  selectedIds: Set<number>
  onToggle: (service: SalonService) => void
}

export function ServiceCard({ card, selectedIds, onToggle }: ServiceCardProps) {
  const [first] = card.options
  const hasOptions = card.options.length > 1
  const prices = card.options.filter((o) => !o.priceOnRequest).map((o) => o.priceNok)
  const fromPrice = Math.min(...prices)

  return (
    <li className="service-card">
      <div className={`service-media ${card.imageUrl ? '' : 'placeholder'}`}>
        {card.imageUrl ? (
          <img
            src={card.imageUrl}
            alt={card.title}
            loading="lazy"
            style={{ objectPosition: `center ${card.imageFocus}%` }}
          />
        ) : (
          <img className="placeholder-logo" src="/images/butterfly-gold.png" alt="" />
        )}
      </div>

      <div className="service-body">
        <div className="service-head">
          <div>
            {card.eyebrow && <p className="eyebrow">{card.eyebrow}</p>}
            <h3>{card.title}</h3>
          </div>
          {prices.length > 0 && (
            <strong className="service-price">
              {hasOptions ? `Fra ${fromPrice}` : first.priceNok} kr
            </strong>
          )}
        </div>

        {hasOptions ? (
          <ul className="options">
            {card.options.map((o) => (
              <li key={o.id} className="option">
                <span className="option-name">
                  {o.name}
                  {o.note && <small className="service-note">{o.note}</small>}
                </span>
                <span className="option-duration">ca. {o.durationMinutes} min</span>
                {o.priceOnRequest ? (
                  <span className="option-contact">Kontakt oss for pris</span>
                ) : (
                  <>
                    <strong className="option-price">{o.priceNok} kr</strong>
                    <ToggleButton selected={selectedIds.has(o.id)} onClick={() => onToggle(o)} />
                  </>
                )}
              </li>
            ))}
          </ul>
        ) : first ? (
          <>
            {first.description && <p className="service-description">{first.description}</p>}
            <p className="option-duration">ca. {first.durationMinutes} min</p>
            {first.note && <small className="service-note">{first.note}</small>}
            {first.priceOnRequest ? (
              <p className="option-contact">Kontakt oss for pris</p>
            ) : (
              <ToggleButton selected={selectedIds.has(first.id)} onClick={() => onToggle(first)} />
            )}
          </>
        ) : null}
      </div>
    </li>
  )
}

function ToggleButton({ selected, onClick }: { selected: boolean; onClick: () => void }) {
  return (
    <button type="button" className={`add-button ${selected ? 'selected' : ''}`} aria-pressed={selected} onClick={onClick}>
      {selected ? '✓ Valgt' : 'Legg til'}
    </button>
  )
}
