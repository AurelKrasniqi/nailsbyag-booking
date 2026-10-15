-- The order of the treatments on the website, set by the admin. Existing rows keep their current (id) order.
ALTER TABLE salon_service ADD COLUMN sort_order INT NOT NULL DEFAULT 0;
UPDATE salon_service SET sort_order = id;
ALTER TABLE salon_service ALTER COLUMN sort_order DROP DEFAULT;
