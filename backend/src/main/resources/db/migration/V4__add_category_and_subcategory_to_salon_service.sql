-- category: top-level group (e.g. "Manikyr"), NULL = standalone service
-- subcategory: optional second level inside a category (e.g. "Nytt sett")
ALTER TABLE salon_service
    ADD COLUMN category VARCHAR(100),
    ADD COLUMN subcategory VARCHAR(100);
