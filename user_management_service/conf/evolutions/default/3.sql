# --- !Ups

create table "wallets" (
    "user_id" BIGSERIAL NOT NULL PRIMARY KEY,
    "balance" NUMERIC NOT NULL,

    constraint "user_fk"
        foreign key ("user_id")
        REFERENCES "users" ("id")
        ON DELETE CASCADE,

    constraint "balance_nonnegative"
        check ("balance" >= 0)
);

# --- !Downs

drop table if exists "wallets" cascade;
