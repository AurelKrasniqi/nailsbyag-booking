import { BUSINESS } from '../business'

const ADDRESS = BUSINESS.address
const PHONE_DISPLAY = BUSINESS.phoneDisplay
const PHONE_LINK = BUSINESS.phoneLink
const INSTAGRAM_URL = BUSINESS.instagramUrl

// Provisional opening hours. Later these should come from the backend (OpeningHours) so admin can edit them.
const openingHours = [
  { days: 'Mandag – Fredag', hours: '08:00 – 22:00' },
  { days: 'Lørdag', hours: '09:00 – 18:00' },
  { days: 'Søndag', hours: '12:00 – 18:00' },
]

// Embed URL without an API key; the plain link is a fallback and opens the real Google Maps.
const mapEmbedUrl = `https://maps.google.com/maps?q=${encodeURIComponent(ADDRESS)}&output=embed`
const mapLinkUrl = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(ADDRESS)}`

function Icon({ children }: { children: React.ReactNode }) {
  return (
    <svg
      className="contact-icon"
      viewBox="0 0 24 24"
      width="18"
      height="18"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.6"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {children}
    </svg>
  )
}

export function SiteFooter() {
  return (
    <footer className="footer" id="kontakt">
      <div className="footer-grid">
        <div>
          <h2 className="footer-brand">
            <img src="/images/butterfly-gold.png" alt="" />
            The Beauty Room
          </h2>
          <p>Ta deg tid til deg selv. Hos oss i Drammen får du omsorg, detaljer og et resultat du vil vise frem.</p>
        </div>

        <div>
          <h2>Åpningstider</h2>
          <ul className="hours">
            {openingHours.map((row) => (
              <li key={row.days}>
                <span>{row.days}</span>
                <span>{row.hours}</span>
              </li>
            ))}
          </ul>
        </div>

        <div>
          <h2>Kontakt oss</h2>
          <ul className="contact-list">
            <li>
              <Icon>
                <path d="M12 21s-7-6.2-7-11a7 7 0 1 1 14 0c0 4.8-7 11-7 11z" />
                <circle cx="12" cy="10" r="2.5" />
              </Icon>
              <span>{ADDRESS}</span>
            </li>
            <li>
              <Icon>
                <path d="M5 4h4l2 5-2.5 1.5a11 11 0 0 0 5 5L15 13l5 2v4a2 2 0 0 1-2 2A16 16 0 0 1 3 6a2 2 0 0 1 2-2z" />
              </Icon>
              <a href={PHONE_LINK}>{PHONE_DISPLAY}</a>
            </li>
            <li>
              <Icon>
                <rect x="3" y="3" width="18" height="18" rx="5" />
                <circle cx="12" cy="12" r="4" />
                <circle cx="17.5" cy="6.5" r="0.8" fill="currentColor" />
              </Icon>
              <a href={INSTAGRAM_URL} target="_blank" rel="noopener noreferrer">
                @thebeautyroom.byag
              </a>
            </li>
          </ul>
        </div>
      </div>

      <div className="map">
        <iframe
          title={`Kart over ${ADDRESS}`}
          src={mapEmbedUrl}
          loading="lazy"
          referrerPolicy="no-referrer-when-downgrade"
          allowFullScreen
        />
      </div>
      <p className="map-link">
        <a href={mapLinkUrl} target="_blank" rel="noopener noreferrer">
          Åpne i Google Maps
        </a>
      </p>

      <p className="copyright">
        © {new Date().getFullYear()} {BUSINESS.tradeName} · {BUSINESS.legalName}, org.nr. {BUSINESS.orgNumber}
      </p>
      <p className="footer-links">
        <a href="#/vilkar">Salgsvilkår</a>
        <a href="#/personvern">Personvern</a>
      </p>
    </footer>
  )
}
