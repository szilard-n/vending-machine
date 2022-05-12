CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE t_role (
    id          UUID PRIMARY KEY,
    name        VARCHAR UNIQUE NOT NULL
);

CREATE TABLE t_user (
    id              UUID PRIMARY KEY,
    username        VARCHAR NOT NULL UNIQUE,
    password        VARCHAR NOT NULL,
    deposit         INTEGER DEFAULT 0
);

CREATE TABLE t_user_role (
    user_id         UUID REFERENCES t_user (id),
    role_id         UUID REFERENCES t_role (id),

    CONSTRAINT user_role_pk
            PRIMARY KEY (user_id, role_id)
);