-- How the customer pays and how far the payment has come.
--   payment_method: AT_SALON (pay when visiting) or VIPPS (pay online when booking)
--   payment_status: UNPAID (at salon), PENDING (waiting for Vipps), PAID, FAILED, REFUNDED
-- An online booking starts as PENDING_PAYMENT: the time is held until payment_expires_at, then released.
-- public_token is a random id the customer gets back: it is also the payment reference sent to Vipps,
-- and it lets the customer's browser ask "was my payment OK?" without guessable numeric ids.
ALTER TABLE appointment
    ADD COLUMN payment_method VARCHAR(20) NOT NULL DEFAULT 'AT_SALON',
    ADD COLUMN payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    ADD COLUMN public_token VARCHAR(36),
    ADD COLUMN payment_expires_at TIMESTAMP;

UPDATE appointment SET public_token = gen_random_uuid()::text;

ALTER TABLE appointment ALTER COLUMN public_token SET NOT NULL;
ALTER TABLE appointment ADD CONSTRAINT appointment_public_token_key UNIQUE (public_token);

-- The application always sets these explicitly, so no silent defaults for new rows
ALTER TABLE appointment ALTER COLUMN payment_method DROP DEFAULT;
ALTER TABLE appointment ALTER COLUMN payment_status DROP DEFAULT;
