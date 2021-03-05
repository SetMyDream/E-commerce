# --- !Ups

create table "products" (
    "id" BIGSERIAL NOT NULL PRIMARY KEY,
    "producttitle" VARCHAR(27) NOT NULL,
    "description" VARCHAR(200) NOT NULL,
    "userId" BIGSERIAL NOT NULL,
)

# --- !Downs

drop table "products" if exists;
