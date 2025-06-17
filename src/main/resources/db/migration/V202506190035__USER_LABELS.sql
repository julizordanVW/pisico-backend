CREATE TABLE user_labels
(
    user_id        UUID                    NOT NULL,
    label_id       UUID                    NOT NULL,

    PRIMARY KEY (user_id, label_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (label_id) REFERENCES labels (id) ON DELETE CASCADE,
    row_created_on timestamp DEFAULT now() NOT NULL
);