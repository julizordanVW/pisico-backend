CREATE TABLE amenities
(
    id             UUID PRIMARY KEY,
    name           VARCHAR(255) UNIQUE     NOT NULL,
    slug           VARCHAR(255) UNIQUE     NOT NULL,
    row_created_on timestamp DEFAULT now() NOT NULL
);