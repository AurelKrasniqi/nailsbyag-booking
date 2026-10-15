-- A "box" on the website (service group) is now a thing of its own, so the admin can give it a title and a photo
-- and add new ones. Before, a box was only the text (category / subcategory) the treatments had in common.
--   title:         the big heading of the box, e.g. "Nytt sett" or "Pedikyr"
--   eyebrow:       small text above the title, e.g. "Manikyr" above "Nytt sett". NULL = none.
--   default_image: a photo that ships with the website (used until the admin uploads her own). NULL = none.
--   image_focus:   which part of a tall photo stays visible when it is cropped to the box: 0 = top, 100 = bottom
--   image_version: NULL = no uploaded photo; otherwise when it was uploaded (cache-busting in the image address)
-- The category / subcategory columns on salon_service stay: the extras ("Tillegg") use them to know what fits what.
CREATE TABLE service_group (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    eyebrow VARCHAR(100),
    sort_order INT NOT NULL,
    default_image VARCHAR(200),
    image_focus INT NOT NULL DEFAULT 40 CHECK (image_focus BETWEEN 0 AND 100),
    image_version BIGINT
);

-- Uploaded photos live in the database, like the "Om oss" photo (the hosting platform wipes the disk on deploy)
CREATE TABLE service_group_image (
    group_id BIGINT PRIMARY KEY REFERENCES service_group (id) ON DELETE CASCADE,
    content_type VARCHAR(30) NOT NULL,
    data BYTEA NOT NULL
);

ALTER TABLE salon_service ADD COLUMN group_id BIGINT REFERENCES service_group (id);

-- One box per category + subcategory (the same grouping the website used), and one per standalone treatment.
-- The boxes keep the order the treatments had.
INSERT INTO service_group (title, eyebrow, sort_order)
SELECT COALESCE(subcategory, category, MIN(name)),
       CASE WHEN subcategory IS NOT NULL THEN category END,
       MIN(sort_order)
FROM salon_service
GROUP BY category, subcategory, CASE WHEN category IS NULL THEN name END;

UPDATE salon_service s
SET group_id = g.id
FROM service_group g
WHERE g.title = COALESCE(s.subcategory, s.category, s.name)
  AND g.eyebrow IS NOT DISTINCT FROM (CASE WHEN s.subcategory IS NOT NULL THEN s.category END);

-- Fails the migration (instead of losing a treatment) if any treatment did not end up in a box
ALTER TABLE salon_service ALTER COLUMN group_id SET NOT NULL;

-- The photos that were hard-coded in the frontend before, now data
UPDATE service_group SET default_image = '/images/services/nytt-sett.png',      image_focus = 50 WHERE title = 'Nytt sett' AND eyebrow = 'Manikyr';
UPDATE service_group SET default_image = '/images/services/pafyll.jpg',         image_focus = 65 WHERE title = 'Påfyll' AND eyebrow = 'Manikyr';
UPDATE service_group SET default_image = '/images/services/pedikyr.jpg',        image_focus = 65 WHERE title = 'Pedikyr' AND eyebrow IS NULL;
UPDATE service_group SET default_image = '/images/services/lashlift.jpg',       image_focus = 75 WHERE title = 'Lashlift' AND eyebrow IS NULL;
UPDATE service_group SET default_image = '/images/services/browlamination.jpg', image_focus = 30 WHERE title = 'Browlamination' AND eyebrow IS NULL;
UPDATE service_group SET default_image = '/images/services/make-up.jpg',        image_focus = 25 WHERE title = 'Make-up' AND eyebrow IS NULL;
