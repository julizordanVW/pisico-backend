CREATE TABLE users
(
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),    
    name                VARCHAR(255)        NOT NULL,
    description         TEXT,
    date_of_birth       DATE,
    email               VARCHAR(255) UNIQUE NOT NULL,
    password_hash       VARCHAR(255)        NOT NULL,
    email_verified      BOOLEAN             NOT NULL DEFAULT false,
    verification_token  VARCHAR(255),
    token_expiry_date   TIMESTAMP,
    phone_number        VARCHAR(50),
    profile_picture_url VARCHAR(255),
    gender              VARCHAR(50),
    role                VARCHAR(50)         NOT NULL DEFAULT 'user',
    last_login          TIMESTAMP,
    time_zone           VARCHAR(50),
    account_status      VARCHAR(50)         NOT NULL DEFAULT 'active',
    row_created_on      timestamp                    DEFAULT now() NOT NULL,
    row_updated_on      timestamp                    DEFAULT now()
);