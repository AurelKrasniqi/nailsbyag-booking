-- An appointment can now contain several treatments (the customer's cart), so the single service_id column
-- is replaced by a join table.
CREATE TABLE appointment_service (
    appointment_id BIGINT NOT NULL REFERENCES appointment (id) ON DELETE CASCADE,
    service_id BIGINT NOT NULL REFERENCES salon_service (id),
    PRIMARY KEY (appointment_id, service_id)
);

INSERT INTO appointment_service (appointment_id, service_id)
SELECT id, service_id FROM appointment;

-- Price is stored at booking time (like end_time) so later price edits do not change old appointments
ALTER TABLE appointment ADD COLUMN total_price_nok INT;

UPDATE appointment a
SET total_price_nok = s.price_nok
FROM salon_service s
WHERE s.id = a.service_id;

ALTER TABLE appointment ALTER COLUMN total_price_nok SET NOT NULL;
ALTER TABLE appointment DROP COLUMN service_id;

-- The database itself refuses double booking: two active appointments for the same employee can never overlap.
-- This is the safety net when two customers press "confirm" on the same time at the same moment.
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE appointment
    ADD CONSTRAINT no_overlapping_appointments
    EXCLUDE USING gist (employee_id WITH =, tsrange(start_time, end_time) WITH &&)
    WHERE (status <> 'CANCELLED');
