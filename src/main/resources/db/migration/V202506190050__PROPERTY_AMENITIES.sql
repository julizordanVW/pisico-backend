CREATE TABLE property_amenities
(
    property_id    UUID                    NOT NULL,
    amenity_id     UUID                    NOT NULL,
    row_created_on timestamp DEFAULT now() NOT NULL,

    PRIMARY KEY (property_id, amenity_id),
    FOREIGN KEY (property_id) REFERENCES properties (id) ON DELETE CASCADE,
    FOREIGN KEY (amenity_id) REFERENCES amenities (id) ON DELETE CASCADE
);