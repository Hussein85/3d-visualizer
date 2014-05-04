# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "MODEL" ("ID" SERIAL NOT NULL PRIMARY KEY,"NAME" VARCHAR(254) NOT NULL,"USER_ID" INTEGER NOT NULL,"DATE" TIMESTAMP NOT NULL,"MATERIAL" VARCHAR(254) NOT NULL,"LOCATION" VARCHAR(254) NOT NULL,"TEXT" VARCHAR(254) NOT NULL,"PATH_OBJECT" VARCHAR(254) NOT NULL,"PATH_TEXTURE" VARCHAR(254) NOT NULL);
create table "TAG" ("ID" SERIAL NOT NULL PRIMARY KEY,"NAME" VARCHAR(254) NOT NULL);
create unique index "IDX_NAME" on "TAG" ("NAME");
create table "TAG_MODEL" ("TAG_ID" INTEGER NOT NULL,"MODEL_ID" INTEGER NOT NULL);
create unique index "IDX_MODEL_TAG" on "TAG_MODEL" ("TAG_ID","MODEL_ID");

# --- !Downs

drop table "MODEL";
drop table "TAG";
drop table "TAG_MODEL";

