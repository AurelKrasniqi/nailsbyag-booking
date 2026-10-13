import { useState } from 'react'

const INSTAGRAM_URL = 'https://www.instagram.com/thebeautyroom.byag/'

interface NavLink {
  label: string
  href: string
  external?: boolean
}

const links: NavLink[] = [
  { label: 'Hjem', href: '#hjem' },
  { label: 'Om oss', href: '#om-oss' },
  { label: 'Behandlinger', href: '#behandlinger' },
  { label: 'Instagram', href: INSTAGRAM_URL, external: true },
  { label: 'Kontakt', href: '#kontakt' },
]

export function SiteHeader() {
  const [menuOpen, setMenuOpen] = useState(false)

  return (
    <header className="site-header">
      <div className="site-header-inner">
        <a className="brand" href="#hjem" aria-label="The Beauty Room, til toppen">
          <img src="/images/butterfly-gold.png" alt="" />
          <span>The Beauty Room</span>
        </a>

        <nav id="site-nav" className={`nav ${menuOpen ? 'open' : ''}`} aria-label="Hovedmeny">
          <ul>
            {links.map((link) => (
              <li key={link.label}>
                <a
                  href={link.href}
                  onClick={() => setMenuOpen(false)}
                  {...(link.external ? { target: '_blank', rel: 'noopener noreferrer' } : {})}
                >
                  {link.label}
                </a>
              </li>
            ))}
          </ul>
        </nav>

        <a className="cta" href="#behandlinger">
          Book nå
        </a>

        <button
          type="button"
          className="menu-toggle"
          aria-expanded={menuOpen}
          aria-controls="site-nav"
          aria-label={menuOpen ? 'Lukk meny' : 'Åpne meny'}
          onClick={() => setMenuOpen((o) => !o)}
        >
          <span />
          <span />
          <span />
        </button>
      </div>
    </header>
  )
}
