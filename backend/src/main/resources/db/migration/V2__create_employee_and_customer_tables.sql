CREATE TABLE employee (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    specialties VARCHAR(300)
);

CREATE TABLE customer (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    email VARCHAR(150)
);
