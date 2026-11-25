import { useEffect, useState } from 'react'
import { fetchTerms, type TermsSection } from './api/terms'
import { BUSINESS } from './business'
import './LegalPages.css'

// DRAFTS. These texts must be read and approved by Agneta (and ideally checked against Forbrukertilsynet's guidance
// or an accountant) before real customers use the site. The parts that are business decisions (cancellation time,
// complaint period) are marked in the source with TODO.

function LegalLayout({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <main className="legal-page">
      <article className="legal-card">
        <a className="legal-back" href="#/">
          ← Til forsiden
        </a>
        <h1>{title}</h1>
        {children}
        <p className="legal-meta">
          {BUSINESS.legalName} (org.nr. {BUSINESS.orgNumber}) · {BUSINESS.tradeName} · {BUSINESS.address}
        </p>
      </article>
    </main>
  )
}

// The sales terms are written by the admin (Admin → Vilkår) and kept in the database. Paragraphs are separated by a blank line.
export function TermsPage() {
  const [sections, setSections] = useState<TermsSection[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchTerms()
      .then(setSections)
      .catch((e: Error) => setError(e.message))
  }, [])

  return (
    <LegalLayout title="Salgsvilkår">
      {error && <p>Kunne ikke hente vilkårene akkurat nå. Prøv igjen senere.</p>}
      {!sections && !error && <p>Laster…</p>}
      {sections?.map((section, i) => (
        <section key={i}>
          {section.heading && <h2>{section.heading}</h2>}
          {section.body.split(/\n\s*\n/).map((paragraph, j) => (
            <p key={j}>{paragraph}</p>
          ))}
        </section>
      ))}
      {sections && (
        <p>
          Hvordan vi bruker opplysningene du gir oss står i <a href="#/personvern">personvernerklæringen</a>.
        </p>
      )}
    </LegalLayout>
  )
}

export function PrivacyPage() {
  return (
    <LegalLayout title="Personvernerklæring">
      <p>
        {BUSINESS.legalName} (org.nr. {BUSINESS.orgNumber}) er ansvarlig for personopplysningene du gir oss når du
        bestiller en time hos {BUSINESS.tradeName}.
      </p>

      <h2>Hva vi lagrer</h2>
      <ul>
        <li>Navn og telefonnummer, og e-post hvis du fyller det inn.</li>
        <li>Hvilken behandling og hvilket tidspunkt du har bestilt, og prisen.</li>
        <li>Kommentaren du eventuelt skriver til oss (for eksempel ønsker eller allergier).</li>
        <li>Om timen er betalt, og betalingsreferansen hvis du betaler med Vipps. Vi lagrer ikke kortnummer.</li>
      </ul>

      <h2>Hvorfor</h2>
      <p>
        Vi bruker opplysningene for å gjennomføre timen du har bestilt, kontakte deg om den, og oppfylle krav i
        regnskaps- og skattelovgivningen. Grunnlaget er avtalen du inngår med oss når du bestiller.
      </p>

      <h2>Hvor lenge</h2>
      {/* TODO (Agneta): confirm how long customer details are kept. */}
      <p>
        Vi beholder bestillingen så lenge det trengs for å gjennomføre timen og for regnskap (opptil 5 år). Du kan be
        om at andre opplysninger slettes.
      </p>

      <h2>Hvem får opplysningene</h2>
      <ul>
        <li>Vi deler ikke opplysningene dine med andre til markedsføring.</li>
        <li>Vipps MobilePay får det som trengs for å gjennomføre betalingen, hvis du betaler med Vipps.</li>
        <li>Nettsiden og databasen ligger hos en leverandør av drift (hosting) som behandler data for oss.</li>
        <li>
          Siden bruker kartet og skrifttypene til Google, og Google kan da motta din IP-adresse når du åpner siden.
        </li>
      </ul>

      <h2>Informasjonskapsler</h2>
      <p>Nettsiden bruker ikke informasjonskapsler (cookies) til sporing eller reklame.</p>

      <h2>Dine rettigheter</h2>
      <p>
        Du kan be om innsyn i, retting av eller sletting av opplysningene vi har om deg. Ta kontakt på telefon{' '}
        <a href={BUSINESS.phoneLink}>{BUSINESS.phoneDisplay}</a>. Du kan også klage til Datatilsynet.
      </p>
    </LegalLayout>
  )
}
