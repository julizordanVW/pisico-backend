CREATE TABLE photos
(
    id             UUID PRIMARY KEY,
    property_id    VARCHAR(255)            NOT NULL,
    url            VARCHAR(2048)           NOT NULL,
    `order`        INT,
    row_created_on timestamp DEFAULT now() NOT NULL,
    
    FOREIGN KEY (property_id) REFERENCES properties (id) ON DELETE CASCADE
);