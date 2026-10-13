-- Settings for the business, always exactly one row (id = 1).
--   vat_registered:   whether the business is registered for VAT (mva). If not, no VAT is deducted from income.
--   vat_rate_percent: the VAT rate. Beauty services are normally 25 %.
-- Prices on the website are consumer prices, i.e. they INCLUDE VAT when the business is VAT registered.
-- Default is registered at 25 %: TODO confirm with AG (and an accountant) whether she is VAT registered.
CREATE TABLE business_settings (
    id BIGINT PRIMARY KEY CHECK (id = 1),
    vat_registered BOOLEAN NOT NULL,
    vat_rate_percent INT NOT NULL CHECK (vat_rate_percent BETWEEN 0 AND 100)
);

INSERT INTO business_settings (id, vat_registered, vat_rate_percent) VALUES (1, TRUE, 25);
