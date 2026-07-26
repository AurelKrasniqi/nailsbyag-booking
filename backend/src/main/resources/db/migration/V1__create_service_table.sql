CREATE TABLE salon_service (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    duration_minutes INT NOT NULL,
    price_nok INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);