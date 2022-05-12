CREATE TABLE t_authentication (
    id                  UUID PRIMARY KEY,
    username            VARCHAR NOT NULL,
    token               VARCHAR NOT NULL,
    expiration_date     TIMESTAMP NOT NULL
);