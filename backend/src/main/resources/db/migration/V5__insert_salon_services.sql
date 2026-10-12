-- Prices from AG. Durations are placeholders: TODO confirm with AG.
INSERT INTO salon_service (name, description, category, subcategory, duration_minutes, price_nok, active)
VALUES
    -- Manikyr / Nytt sett
    ('Nude/Ensfarget',          NULL, 'Manikyr', 'Nytt sett', 105,  750, TRUE),
    ('Enkel design/French tip', NULL, 'Manikyr', 'Nytt sett', 120, 1000, TRUE),
    ('Ombre',                   NULL, 'Manikyr', 'Nytt sett', 135, 1100, TRUE),
    ('Avansert design',         NULL, 'Manikyr', 'Nytt sett', 150, 1150, TRUE),
    -- Manikyr / Påfyll (etter 3-4 uker)
    ('Nude/Ensfarget',          NULL, 'Manikyr', 'Påfyll',     75,  600, TRUE),
    ('Enkel design/French tip', NULL, 'Manikyr', 'Påfyll',     90,  850, TRUE),
    ('Ombre',                   NULL, 'Manikyr', 'Påfyll',    105,  950, TRUE),
    ('Avansert design',         NULL, 'Manikyr', 'Påfyll',    120, 1000, TRUE),
    -- Pedikyr
    ('Pedikyr en farge',                     NULL, 'Pedikyr', NULL,  60,  650, TRUE),
    ('Pedikyr fransk',                       NULL, 'Pedikyr', NULL,  75,  750, TRUE),
    ('Pedikyr full pakke med rens av hæl',   NULL, 'Pedikyr', NULL,  90, 1300, TRUE),
    -- Microblading
    ('Første gang', NULL, 'Microblading', NULL, 150, 2700, TRUE),
    ('Påfyll',      NULL, 'Microblading', NULL,  90, 1500, TRUE),
    -- Standalone
    ('Lashlift',       NULL, NULL, NULL, 75, 650, TRUE),
    ('Browlamination', NULL, NULL, NULL, 60, 750, TRUE);
