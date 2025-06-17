CREATE TABLE user_properties
(
    user_id        UUID                      NOT NULL,
    property_id    UUID                      NOT NULL,
    role           VARCHAR(50) DEFAULT 'user',
    joined_at      TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    row_created_on timestamp   DEFAULT now() NOT NULL,
    row_updated_on timestamp   DEFAULT now(),
    
    PRIMARY KEY (user_id, property_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (property_id) REFERENCES properties (id) ON DELETE CASCADE
);