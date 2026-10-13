-- Optional free-text note from the customer when booking (e.g. "bring own inspiration photos", allergies)
ALTER TABLE appointment
    ADD COLUMN customer_comment VARCHAR(500);
