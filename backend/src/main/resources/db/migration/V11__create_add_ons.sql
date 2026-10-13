-- Extras ("Tillegg" on the price list) that a customer can add to a treatment.
--   category / subcategory: which treatments the extra belongs to. subcategory NULL = every treatment in the category;
--                           "Endring av form (påfyll)" only makes sense for a refill, so it is limited to "Påfyll".
--   unit_label:   what the price is per ("pr. negl"), NULL for a one-off extra
--   max_quantity: 10 = can be chosen per nail (up to ten nails), 1 = either yes or no
-- Prices are from AG's price list. Durations are placeholders: TODO confirm with AG.
CREATE TABLE add_on (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    unit_label VARCHAR(30),
    category VARCHAR(100) NOT NULL,
    subcategory VARCHAR(100),
    price_nok INT NOT NULL,
    duration_minutes INT NOT NULL,
    max_quantity INT NOT NULL CHECK (max_quantity >= 1),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL
);

INSERT INTO add_on (name, unit_label, category, subcategory, price_nok, duration_minutes, max_quantity, sort_order)
VALUES
    ('Glitter/Chrome',                                   'pr. negl', 'Manikyr', NULL,     15,  1, 10, 1),
    ('Stener/Charms',                                    'pr. negl', 'Manikyr', NULL,     20,  2, 10, 2),
    ('Fiks av negl',                                     'pr. negl', 'Manikyr', NULL,     35, 10, 10, 3),
    ('Ekstra large negler',                              NULL,       'Manikyr', NULL,    100, 15,  1, 4),
    ('Endring av form',                                  NULL,       'Manikyr', 'Påfyll', 100, 15,  1, 5),
    ('Fjerning av alle negler (inkl. rens av neglbånd)', NULL,       'Manikyr', NULL,    300, 30,  1, 6);

-- The extras a customer chose for one appointment. Name, price and duration are copied at booking time
-- (like total_price_nok and end_time) so later edits to the price list do not change old appointments.
-- price_nok and duration_minutes are the totals for the line (unit price x quantity).
CREATE TABLE appointment_add_on (
    appointment_id BIGINT NOT NULL REFERENCES appointment (id) ON DELETE CASCADE,
    add_on_id BIGINT NOT NULL REFERENCES add_on (id),
    name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL CHECK (quantity >= 1),
    price_nok INT NOT NULL,
    duration_minutes INT NOT NULL,
    PRIMARY KEY (appointment_id, add_on_id)
);
