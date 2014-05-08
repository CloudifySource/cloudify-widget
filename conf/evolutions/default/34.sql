create table mail_chimp_details (
  id                        bigint auto_increment not null,
  api_key                   varchar(255),
  list_id                   varchar(255),
  enabled                   tinyint(1) default 0,
  constraint pk_mail_chimp_details primary key (id))
;


alter table widget add column  mail_chimp_details_id     bigint;


-- drop table mail_chimp_details
-- alter table widget drop column mail_chimp_details_id