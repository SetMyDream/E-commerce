# --- !Ups

create table "login_info" (
    "id" BIGSERIAL NOT NULL PRIMARY KEY,
    "provider_id" VARCHAR(20) NOT NULL,
    "provider_key" BIGSERIAL NOT NULL,

    constraint "user_fk"
        foreign key ("provider_key")
        REFERENCES "users" ("id")
        ON DELETE CASCADE
);

create table "password_info" (
    "login_info_id" BIGSERIAL NOT NULL PRIMARY KEY,
    "hasher" VARCHAR(20) NOT NULL,
    "password" VARCHAR(100) NOT NULL,
    "salt" VARCHAR(100),

    constraint "login_info_fk"
        foreign key ("login_info_id")
        REFERENCES "login_info" ("id")
        ON DELETE CASCADE
);

# --- !Downs

drop table if exists "login_info" cascade;
drop table if exists "password_info" cascade;
