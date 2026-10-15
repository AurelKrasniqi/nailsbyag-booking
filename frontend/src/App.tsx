import { useEffect, useState } from 'react'
import { DEFAULT_ABOUT_IMAGE, fetchAbout, type About } from './api/about'
import { fetchAddOns, type AddOn } from './api/addOns'
import { fetchSalonServices, type SalonService } from './api/salonServices'
import { fetchServiceGroups, type ServiceGroup } from './api/serviceGroups'
import { eligibleAddOns } from './cart'
import { AboutSection } from './components/AboutSection'
import { CartBar } from './components/CartBar'
import { ServiceCard } from './components/ServiceCard'
import { SiteFooter } from './components/SiteFooter'
import { SiteHeader } from './components/SiteHeader'
import { groupServices } from './groupServices'
import './App.css'

function App() {
  const [services, setServices] = useState<SalonService[]>([])
  const [groups, setGroups] = useState<ServiceGroup[]>([])
  const [about, setAbout] = useState<About | null>(null)
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

  // Extras ("Tillegg") such as glitter or charms. quantities = how many of each the customer has picked, by extra id.
  // Only extras that fit the chosen treatments are offered; if the list cannot be loaded the site works without extras.
  const [allAddOns, setAllAddOns] = useState<AddOn[]>([])
  const [quantities, setQuantities] = useState<Record<number, number>>({})
  const extras = eligibleAddOns(allAddOns, selected)

  function changeQuantity(addOnId: number, quantity: number) {
    setQuantities((current) => ({ ...current, [addOnId]: quantity }))
  }

  // If the text cannot be loaded the box is left out; the rest of the site still works
  useEffect(() => {
    fetchAbout()
      .then(setAbout)
      .catch(() => setAbout(null))
  }, [])

  useEffect(() => {
    fetchAddOns()
      .then(setAllAddOns)
      .catch(() => setAllAddOns([]))
  }, [])

  useEffect(() => {
    Promise.all([fetchSalonServices(), fetchServiceGroups()])
      .then(([all, boxes]) => {
        setServices(all.filter((s) => s.active))
        setGroups(boxes)
      })
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  return (
    <>
      <SiteHeader />

      <section className="hero" id="hjem">
        <div className="hero-inner">
          <img className="logo" src="/images/butterfly-gold.png" alt="The Beauty Room by AG logo" />
          <h1>The Beauty Room</h1>
          <p className="tagline">by AG · Drammen</p>
          <p className="offers">Russian manicure &amp; pedicure · Lash lift · Brow lamination</p>
          <a className="button" href="#behandlinger">
            Se behandlinger
          </a>
        </div>
      </section>

      {about && (
        <section className="container about" id="om-oss">
          <h2 className="section-title">Om oss</h2>
          <AboutSection
            heading={about.heading}
            body={about.body}
            imageSrc={about.imageUrl ?? DEFAULT_ABOUT_IMAGE}
          />
        </section>
      )}

      <main className="container" id="behandlinger">
        <h2 className="section-title">Behandlinger</h2>

        {loading && <p className="status">Laster behandlinger…</p>}
        {error && <p className="error">Kunne ikke hente behandlinger: {error}</p>}
        {!loading && !error && services.length === 0 && (
          <p className="status">Ingen behandlinger er lagt inn ennå.</p>
        )}

        <ul className="cards">
          {groupServices(groups, services).map((card) => (
            <ServiceCard key={card.groupId} card={card} selectedIds={selectedIds} onToggle={toggleService} />
          ))}
        </ul>
      </main>

      <SiteFooter />

      <CartBar
        items={selected}
        extras={extras}
        quantities={quantities}
        onQuantityChange={changeQuantity}
        onRemove={(id) => setSelected((current) => current.filter((s) => s.id !== id))}
        onClear={() => {
          setSelected([])
          setQuantities({})
        }}
      />
    </>
  )
}

export default App
