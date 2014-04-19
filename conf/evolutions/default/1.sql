# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "TAG" ("ID" SERIAL NOT NULL PRIMARY KEY,"NAME" VARCHAR(254) NOT NULL);
create unique index "IDX_NAME" on "TAG" ("NAME");

# --- !Downs

drop table "TAG";

