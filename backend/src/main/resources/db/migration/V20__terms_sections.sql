-- The "Salgsvilkår" page, editable by the admin. Each row is a block (optional heading + text) in sort order.
-- body: paragraphs separated by a blank line. These are the texts that used to be written in the website code.
CREATE TABLE terms_section (
    id BIGSERIAL PRIMARY KEY,
    heading VARCHAR(100),
    body TEXT NOT NULL,
    sort_order INT NOT NULL
);

INSERT INTO terms_section (heading, body, sort_order) VALUES
(NULL,
 'Disse vilkårene gjelder når du bestiller en time hos The Beauty Room by AG på nettsiden. Selger er BYCI BEAUTYROOM, organisasjonsnummer 935418658, Kleivene 131, 3041 Drammen.',
 0),
('Hva du bestiller',
 'Du velger behandlinger og eventuelle tillegg, og en ledig tid. Pris og varighet vises før du bestiller, og prisene er oppgitt i norske kroner inkludert merverdiavgift. Tilleggene (for eksempel glitter, stener eller endring av form) kan gjøre timen lengre, og det vises i kalenderen.',
 1),
('Betaling',
 'Du betaler enten i salongen når du kommer til timen, eller på forhånd på nettsiden med Vipps når den muligheten er tilgjengelig. Betaler du med Vipps, holdes tiden av i 10 minutter mens du betaler. Fullføres ikke betalingen, frigis tiden og ingen penger trekkes.',
 2),
('Avbestilling og endring',
 'Du kan avbestille timen gratis inntil 24 timer før. Ta kontakt på telefon +47 401 73 963 eller via Instagram (@thebeautyroom.byag). Har du betalt med Vipps og avbestiller i tide, får du pengene tilbake til samme betalingsmiddel. Avbestilles timen senere, eller du ikke møter opp, kan vi kreve betaling for timen.',
 3),
('Angrerett',
 'Timen er bestilt til et bestemt tidspunkt, og avbestilling skjer etter reglene over. Har du spørsmål om dine rettigheter som forbruker, kan du kontakte oss eller Forbrukertilsynet.',
 4),
('Reklamasjon og klager',
 'Er du ikke fornøyd med resultatet, ta kontakt så raskt som mulig, helst innen 7 dager, så finner vi en løsning, for eksempel retting av behandlingen. Du kan kontakte oss på telefon +47 401 73 963. Vi svarer så fort vi kan. Kommer vi ikke til enighet, kan du ta saken videre til Forbrukertilsynet eller Forbrukerrådet.',
 5),
('Retur',
 'Behandlinger er tjenester og kan ikke leveres tilbake. Se reklamasjon over hvis noe ikke er som avtalt.',
 6);
