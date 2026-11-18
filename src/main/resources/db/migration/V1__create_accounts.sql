CREATE TABLE accounts (
    id          UUID           PRIMARY KEY,
    owner_name  VARCHAR(100)   NOT NULL,
    currency    VARCHAR(3)     NOT NULL,
    balance     NUMERIC(19, 2) NOT NULL CHECK (balance >= 0),
    created_at  TIMESTAMPTZ    NOT NULL
);
