CREATE TABLE properties
(
    id             UUID PRIMARY KEY,
    name           VARCHAR(255) UNIQUE NOT NULL,
    description    TEXT,
    type           VARCHAR(255)        NOT NULL CHECK (type IN ('room', 'apartment', 'house', 'studio', 'chalet', 'duplex')),
    price          DECIMAL(10, 2)      NOT NULL,
    rooms          INT                 NOT NULL,
    bathrooms      INT                 NOT NULL,
    roommates      INT,
    furnished      BOOLEAN             NOT NULL DEFAULT FALSE,
    address        VARCHAR(255)        NOT NULL,
    city           VARCHAR(255)        NOT NULL,
    postal_code    VARCHAR(20),
    country        VARCHAR(100)        NOT NULL,
    latitude       VARCHAR(255),
    longitude      VARCHAR(255),
    row_created_on timestamp                    DEFAULT now() NOT NULL,
    row_updated_on timestamp                    DEFAULT now()
);