-- An extra ("Tillegg") now says which single treatments it can be added to, instead of a whole box (V17).
-- Example: "Farge på vipper" belongs on Vippeløft and Vippeløft & Brynslaminering, but not on "Kun farging av vipper".
CREATE TABLE add_on_service (
    add_on_id BIGINT NOT NULL REFERENCES add_on (id) ON DELETE CASCADE,
    service_id BIGINT NOT NULL REFERENCES salon_service (id) ON DELETE CASCADE,
    PRIMARY KEY (add_on_id, service_id)
);

-- Start from what works today: every treatment in a box the extra was linked to. The admin can then untick
-- the treatments that should not have it.
INSERT INTO add_on_service (add_on_id, service_id)
SELECT g.add_on_id, s.id
FROM add_on_group g
JOIN salon_service s ON s.group_id = g.group_id;

DROP TABLE add_on_group;
