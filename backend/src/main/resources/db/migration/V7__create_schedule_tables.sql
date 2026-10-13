-- Weekly recurring opening hours per employee. Several rows per weekday are allowed (e.g. a lunch break).
CREATE TABLE opening_hours (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee (id),
    day_of_week VARCHAR(10) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    CHECK (end_time > start_time)
);

-- One-off changes on a specific date.
-- BLOCKED = unavailable (times NULL = whole day), EXTRA_OPEN = open outside the weekly hours (times required).
CREATE TABLE schedule_exception (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee (id),
    date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    type VARCHAR(20) NOT NULL,
    CHECK ((start_time IS NULL) = (end_time IS NULL)),
    CHECK (end_time IS NULL OR end_time > start_time),
    CHECK (type = 'BLOCKED' OR start_time IS NOT NULL)
);

-- AG works alone, so she is the only employee. Hours are placeholders: TODO confirm with AG.
INSERT INTO employee (name, specialties) VALUES ('Agneta', 'Manikyr, pedikyr, lashlift, browlamination, microblading');

INSERT INTO opening_hours (employee_id, day_of_week, start_time, end_time)
SELECT e.id, d.day, TIME '10:00', TIME '18:00'
FROM employee e, (VALUES ('MONDAY'), ('TUESDAY'), ('WEDNESDAY'), ('THURSDAY'), ('FRIDAY')) AS d (day)
WHERE e.name = 'Agneta';
