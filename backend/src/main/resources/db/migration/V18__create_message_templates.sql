-- The texts sent to customers, editable by Agneta in the admin page. Always exactly one row (id = 1), like business_settings.
-- Placeholders in curly braces ({navn}, {dato}, {tid}, {behandling}, {pris}) are filled in when the text is sent.
-- The defaults below are the wording that was hardcoded before; the same defaults live in MessageTemplateService
-- so the admin page can offer "Tilbakestill til standard".
CREATE TABLE message_templates (
    id BIGINT PRIMARY KEY CHECK (id = 1),
    confirmation_text TEXT NOT NULL,
    reminder_text TEXT NOT NULL
);

INSERT INTO message_templates (id, confirmation_text, reminder_text) VALUES (
    1,
    'Hei! Takk for at du booket time hos oss på thebeautyroombyag. Dette er en bekreftelse på bookingen din {dato} kl. {tid}. Vi ses!',
    'Påminnelse: du har time hos thebeautyroombyag i morgen kl. {tid} ({dato}). Vi gleder oss til å se deg!'
);
