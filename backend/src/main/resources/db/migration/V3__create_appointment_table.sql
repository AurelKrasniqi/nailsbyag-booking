CREATE TABLE appointment (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer (id),
    employee_id BIGINT NOT NULL REFERENCES employee (id),
    service_id BIGINT NOT NULL REFERENCES salon_service (id),
    start_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL
);
