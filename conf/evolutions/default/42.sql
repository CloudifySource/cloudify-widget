alter table server_node drop column recipe_properties;
alter table server_node drop column advanced_params;

alter table server_node add column execution_data longtext;


create table aws_image_share (
  id                        bigint auto_increment not null,
  image_id                  varchar(255),
  api_key                   varchar(255),
  api_secret_key            varchar(255),
  widget_id                 bigint,
  constraint pk_aws_image_share primary key (id))
;

drop table mandrill_details;


 create table install_finished_email_details (
  id                        bigint auto_increment not null,
  enabled                   tinyint(1) default 0,
  data                      longtext,
  widget_id                 bigint,
  constraint pk_install_finished_email_detail primary key (id))
;

alter table widget drop column mandrill_details_id;

alter table server_node add column    random_password           varchar(255);