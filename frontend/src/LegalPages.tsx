import { BUSINESS, FREE_CANCELLATION_HOURS } from './business'
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

export function TermsPage() {
  return (
    <LegalLayout title="Salgsvilkår">
      <p>
        Disse vilkårene gjelder når du bestiller en time hos {BUSINESS.tradeName} på nettsiden. Selger er{' '}
        {BUSINESS.legalName}, organisasjonsnummer {BUSINESS.orgNumber}, {BUSINESS.address}.
      </p>

      <h2>Hva du bestiller</h2>
      <p>
        Du velger behandlinger og eventuelle tillegg, og en ledig tid. Pris og varighet vises før du bestiller, og
        prisene er oppgitt i norske kroner inkludert merverdiavgift. Tilleggene (for eksempel glitter, stener eller
        endring av form) kan gjøre timen lengre, og det vises i kalenderen.
      </p>

      <h2>Betaling</h2>
      <p>
        Du betaler enten i salongen når du kommer til timen, eller på forhånd på nettsiden med Vipps når den
        muligheten er tilgjengelig. Betaler du med Vipps, holdes tiden av i 10 minutter mens du betaler. Fullføres
        ikke betalingen, frigis tiden og ingen penger trekkes.
      </p>

      <h2>Avbestilling og endring</h2>
      {/* TODO (Agneta): confirm the cancellation time and what happens after it. */}
      <p>
        Du kan avbestille timen gratis inntil {FREE_CANCELLATION_HOURS} timer før. Ta kontakt på telefon{' '}
        <a href={BUSINESS.phoneLink}>{BUSINESS.phoneDisplay}</a> eller via Instagram ({BUSINESS.instagramHandle}). Har
        du betalt med Vipps og avbestiller i tide, får du pengene tilbake til samme betalingsmiddel. Avbestilles
        timen senere, eller du ikke møter opp, kan vi kreve betaling for timen.
      </p>

      <h2>Angrerett</h2>
      <p>
        Timen er bestilt til et bestemt tidspunkt, og avbestilling skjer etter reglene over. Har du spørsmål om dine
        rettigheter som forbruker, kan du kontakte oss eller Forbrukertilsynet.
      </p>

      <h2>Reklamasjon og klager</h2>
      {/* TODO (Agneta): confirm the period and how she wants to be contacted. */}
      <p>
        Er du ikke fornøyd med resultatet, ta kontakt så raskt som mulig, helst innen 7 dager, så finner vi en
        løsning, for eksempel retting av behandlingen. Du kan kontakte oss på telefon{' '}
        <a href={BUSINESS.phoneLink}>{BUSINESS.phoneDisplay}</a>. Vi svarer så fort vi kan. Kommer vi ikke til enighet,
        kan du ta saken videre til Forbrukertilsynet eller Forbrukerrådet.
      </p>

      <h2>Retur</h2>
      <p>Behandlinger er tjenester og kan ikke leveres tilbake. Se reklamasjon over hvis noe ikke er som avtalt.</p>

      <h2>Personopplysninger</h2>
      <p>
        Hvordan vi bruker opplysningene du gir oss står i <a href="#/personvern">personvernerklæringen</a>.
      </p>
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
