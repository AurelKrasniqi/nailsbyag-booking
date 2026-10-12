-- end_time is stored (not derived) so old appointments keep their length when a service's duration is edited later
ALTER TABLE appointment
    ADD COLUMN end_time TIMESTAMP;

UPDATE appointment a
SET end_time = a.start_time + (s.duration_minutes * INTERVAL '1 minute')
FROM salon_service s
WHERE s.id = a.service_id;

ALTER TABLE appointment
    ALTER COLUMN end_time SET NOT NULL;
