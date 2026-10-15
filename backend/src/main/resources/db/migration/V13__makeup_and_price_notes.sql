-- note:             small print shown under a treatment, e.g. "Tillegg kan forekomme"
-- price_on_request: the price is not fixed (e.g. bridal make-up). Shown as "kontakt oss", cannot be booked online.
ALTER TABLE salon_service
    ADD COLUMN note VARCHAR(200),
    ADD COLUMN price_on_request BOOLEAN NOT NULL DEFAULT FALSE;

-- From AG. Durations are placeholders: TODO confirm with AG.
INSERT INTO salon_service (name, description, category, subcategory, duration_minutes, price_nok, active, note, price_on_request)
VALUES
    ('Festsminke enkel', NULL, 'Make-up', NULL,  60, 2500, TRUE, 'Tillegg kan forekomme', FALSE),
    ('Full glam',        NULL, 'Make-up', NULL,  90, 3500, TRUE, 'Tillegg kan forekomme', FALSE),
    ('Brudesminke',      NULL, 'Make-up', NULL, 120,    0, TRUE, 'Kontakt oss via tlf, Instagram eller e-post for pris', TRUE);

-- A reminder text message goes out the day before the appointment. NULL = not sent yet.
ALTER TABLE appointment ADD COLUMN reminder_sent_at TIMESTAMP;

-- Existing appointments were booked before reminders existed: never send them one retroactively
UPDATE appointment SET reminder_sent_at = start_time;
