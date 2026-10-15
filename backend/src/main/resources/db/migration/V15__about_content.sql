-- The "Om oss" box on the website, editable by the admin. Always exactly one row (id = 1).
--   body:          paragraphs separated by a blank line
--   image_version: NULL = the default photo that ships with the website; otherwise the time the photo was uploaded,
--                  used as a cache-busting parameter in the image address
CREATE TABLE about_content (
    id BIGINT PRIMARY KEY CHECK (id = 1),
    heading VARCHAR(100) NOT NULL,
    body TEXT NOT NULL,
    image_version BIGINT
);

INSERT INTO about_content (id, heading, body) VALUES (
    1,
    'Møt Agneta',
    'Hei, jeg er Agneta, neglartist og eier av The Beauty Room by AG i Drammen. Jeg utfører russisk manikyr og pedikyr, vippeløft, brynlaminering og microblading, og jobber også som freelance makeupartist.

Jeg tar imot kunder alene, så du får min fulle oppmerksomhet hele timen. Hos meg er det rom for en god prat og litt velfortjent tid for deg selv. Når du går herfra, skal du kjenne deg fin og være stolt av resultatet.'
);

-- The uploaded photo lives in the database (not on disk): the hosting platform wipes the disk on every deploy.
-- Separate table so reading the text never drags the image bytes along.
CREATE TABLE about_image (
    id BIGINT PRIMARY KEY CHECK (id = 1),
    content_type VARCHAR(30) NOT NULL,
    data BYTEA NOT NULL
);
