alter table widget add column send_email  tinyint(1) default 0;
alter table server_node add column widget_instance_user_details_id bigint;

create table widget_instance_user_details (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  last_name                 varchar(255),
  email                     varchar(255),
  constraint pk_widget_instance_user_details primary key (id))
DEFAULT CHARSET=utf8;


create table mandrill_details (
  id                        bigint auto_increment not null,
  api_key                   varchar(255),
  template_name             varchar(255),
  csv_bcc_emails            varchar(255),
  constraint pk_mandrill_details primary key (id))
;


alter table widget add column  mandrill_details_id       bigint;

