import type { ServiceCardData } from '../groupServices'
import { serviceImages } from '../serviceImages'

export function ServiceCard({ card }: { card: ServiceCardData }) {
  const image = serviceImages[card.key]
  const [first] = card.options
  const hasOptions = card.options.length > 1
  const fromPrice = Math.min(...card.options.map((o) => o.priceNok))

  return (
    <li className="service-card">
      <div className={`service-media ${image ? '' : 'placeholder'}`}>
        {image ? (
          <img
            src={image.src}
            alt={card.title}
            loading="lazy"
            style={image.position ? { objectPosition: image.position } : undefined}
          />
        ) : (
          <img className="placeholder-logo" src="/images/logo.png" alt="" />
        )}
      </div>

      <div className="service-body">
        <div className="service-head">
          <div>
            {card.eyebrow && <p className="eyebrow">{card.eyebrow}</p>}
            <h3>{card.title}</h3>
          </div>
          <strong className="service-price">
            {hasOptions ? `Fra ${fromPrice}` : first.priceNok} kr
          </strong>
        </div>

        {hasOptions ? (
          <ul className="options">
            {card.options.map((o) => (
              <li key={o.id} className="option">
                <span className="option-name">{o.name}</span>
                <span className="option-duration">ca. {o.durationMinutes} min</span>
                <strong className="option-price">{o.priceNok} kr</strong>
              </li>
            ))}
          </ul>
        ) : (
          <>
            {first.description && <p className="service-description">{first.description}</p>}
            <p className="option-duration">ca. {first.durationMinutes} min</p>
          </>
        )}
      </div>
    </li>
  )
}
