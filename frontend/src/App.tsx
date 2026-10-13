import { useEffect, useState } from 'react'
import { fetchSalonServices, type SalonService } from './api/salonServices'
import { CartBar } from './components/CartBar'
import { ServiceCard } from './components/ServiceCard'
import { SiteFooter } from './components/SiteFooter'
import { SiteHeader } from './components/SiteHeader'
import { groupServices } from './groupServices'
import './App.css'

function App() {
  const [services, setServices] = useState<SalonService[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // The cart: treatments the customer has clicked. Kept in order of selection.
  const [selected, setSelected] = useState<SalonService[]>([])
  const selectedIds = new Set(selected.map((s) => s.id))

  function toggleService(service: SalonService) {
    setSelected((current) =>
      current.some((s) => s.id === service.id) ? current.filter((s) => s.id !== service.id) : [...current, service],
    )
  }

  useEffect(() => {
    fetchSalonServices()
      .then((all) => setServices(all.filter((s) => s.active)))
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  return (
    <>
      <SiteHeader />

      <section className="hero" id="hjem">
        <div className="hero-inner">
          <img className="logo" src="/images/logo.png" alt="The Beauty Room by AG logo" />
          <h1>The Beauty Room</h1>
          <p className="tagline">by AG · Drammen</p>
          <p className="offers">Russian manicure &amp; pedicure · Lash lift · Brow lamination</p>
          <a className="button" href="#behandlinger">
            Se behandlinger
          </a>
        </div>
      </section>

      <section className="container about" id="om-oss">
        <h2 className="section-title">Om oss</h2>
        <div className="about-card">
          <div className="about-text">
            <h3>Møt Agneta</h3>
            <p>
              Hei, jeg er Agneta, neglartist og eier av The Beauty Room by AG i Drammen. Jeg utfører russisk
              manikyr og pedikyr, vippeløft, brynlaminering og microblading, og jobber også som freelance
              makeupartist.
            </p>
            <p>
              Jeg tar imot kunder alene, så du får min fulle oppmerksomhet hele timen. Hos meg er det rom for en
              god prat og litt velfortjent tid for deg selv. Når du går herfra, skal du kjenne deg fin og være
              stolt av resultatet.
            </p>
          </div>
          <div className="about-image">
            <img src="/images/om-oss.jpg" alt="Arbeidsplassen hos The Beauty Room by AG" loading="lazy" />
          </div>
        </div>
      </section>

      <main className="container" id="behandlinger">
        <h2 className="section-title">Behandlinger</h2>

        {loading && <p className="status">Laster behandlinger…</p>}
        {error && <p className="error">Kunne ikke hente behandlinger: {error}</p>}
        {!loading && !error && services.length === 0 && (
          <p className="status">Ingen behandlinger er lagt inn ennå.</p>
        )}

        <ul className="cards">
          {groupServices(services).map((card) => (
            <ServiceCard key={card.key} card={card} selectedIds={selectedIds} onToggle={toggleService} />
          ))}
        </ul>
      </main>

      <SiteFooter />

      <CartBar
        items={selected}
        onRemove={(id) => setSelected((current) => current.filter((s) => s.id !== id))}
        onClear={() => setSelected([])}
      />
    </>
  )
}

export default App
