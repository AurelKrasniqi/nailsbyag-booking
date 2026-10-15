-- An extra ("Tillegg") now says which boxes (service groups) it can be added to, instead of matching on the
-- category / subcategory text. That way the admin can add a new box or a new extra and choose what fits what.
CREATE TABLE add_on_group (
    add_on_id BIGINT NOT NULL REFERENCES add_on (id) ON DELETE CASCADE,
    group_id BIGINT NOT NULL REFERENCES service_group (id) ON DELETE CASCADE,
    PRIMARY KEY (add_on_id, group_id)
);

-- Same rule as before: the extra fits every box that has a treatment in its category (and subcategory, if it has one)
INSERT INTO add_on_group (add_on_id, group_id)
SELECT DISTINCT a.id, s.group_id
FROM add_on a
JOIN salon_service s ON s.category = a.category AND (a.subcategory IS NULL OR a.subcategory = s.subcategory);

ALTER TABLE add_on DROP COLUMN category;
ALTER TABLE add_on DROP COLUMN subcategory;
