# --- !Ups

create table "users" (
    "id" BIGSERIAL NOT NULL PRIMARY KEY,
    "username" VARCHAR(20) NOT NULL UNIQUE
);

# --- !Downs

drop table if exists "users" cascade;
