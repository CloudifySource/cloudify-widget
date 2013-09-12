create table lead (
  id                        bigint auto_increment not null,
  email                     varchar(255),
  extra                     longtext,
  uuid                      varchar(255),
  owner_id                  bigint,
  validated                 tinyint(1) default 0,
  created_timestamp         bigint,
  trial_timeout_timestamp   bigint,
  constraint pk_lead primary key (id))
;
