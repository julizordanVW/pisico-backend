CREATE TABLE labels
(
    id             UUID PRIMARY KEY,
    name           VARCHAR(255) UNIQUE     NOT NULL,
    slug           VARCHAR(255) UNIQUE     NOT NULL,
    category       VARCHAR(255),
    row_created_on timestamp DEFAULT now() NOT NULL
);