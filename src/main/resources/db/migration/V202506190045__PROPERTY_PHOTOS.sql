CREATE TABLE propierty_photos
(
    id             UUID PRIMARY KEY,
    property_id    UUID                    NOT NULL,
    url            VARCHAR(2048)           NOT NULL,
    sort_order     INT,
    row_created_on timestamp DEFAULT now() NOT NULL,

    FOREIGN KEY (property_id) REFERENCES properties (id) ON DELETE CASCADE
);