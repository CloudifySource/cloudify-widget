drop table lead;

create table lead_details (
  id                        bigint auto_increment not null,
  created                   bigint,
  is_read                      tinyint(1) default 0,
  data                      longtext,
  constraint pk_lead_details primary key (id)
  ) ;


alter table server_node drop column lead_id;

