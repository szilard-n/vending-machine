CREATE TABLE t_product (
    id                  UUID PRIMARY KEY,
    product_name        VARCHAR NOT NULL,
    amount_available    INTEGER NOT NULL DEFAULT 0,
    cost                INTEGER NOT NULL CHECK (cost % 5 = 0),
    seller_id           UUID NOT NULL REFERENCES t_user (id)
);

ALTER TABLE t_user
    ADD CONSTRAINT positive_deposit_num CHECK (deposit >= 0);