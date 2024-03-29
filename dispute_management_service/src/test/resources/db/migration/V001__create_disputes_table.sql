CREATE TABLE "dispute" (
  "id" BIGSERIAL NOT NULL PRIMARY KEY,
  "buyer_id" BIGSERIAL NOT NULL,
  "seller_id" BIGSERIAL NOT NULL,
  "purchase_id" BIGSERIAL NOT NULL,
  "status" CHAR(1) NOT NULL,
  "created" DATE NOT NULL DEFAULT CURRENT_DATE,

  CONSTRAINT unique_agenda UNIQUE (buyer_id, seller_id, purchase_id)
);
