# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table server_node (
  id                        varchar(255) not null,
  server_id                 varchar(255),
  expiration_time           bigint,
  public_ip                 varchar(255),
  private_ip                varchar(255),
  busy                      boolean,
  constraint pk_server_node primary key (id))
;

create table user (
  id                        bigint not null,
  email                     varchar(255),
  password                  varchar(255),
  auth_token                varchar(255),
  expires                   varchar(255),
  constraint pk_user primary key (id))
;

create table widget (
  id                        varchar(255) not null,
  user_id                   bigint not null,
  user_name                 varchar(255),
  product_name              varchar(255),
  provider_url              varchar(255),
  product_version           varchar(255),
  title                     varchar(255),
  youtube_video_url         varchar(255),
  recipe_url                varchar(255),
  allow_anonymous           boolean,
  api_key                   varchar(255),
  launches                  integer,
  enabled                   boolean,
  console_name              varchar(255),
  console_url               varchar(255),
  constraint pk_widget primary key (id))
;

create table widget_instance (
  id                        bigint not null,
  widget_id                 varchar(255) not null,
  instance_id               varchar(255),
  anonymouse                boolean,
  public_ip                 varchar(255),
  constraint pk_widget_instance primary key (id))
;

create sequence server_node_seq;

create sequence user_seq;

create sequence widget_seq;

create sequence widget_instance_seq;

alter table widget add constraint fk_widget_user_1 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_widget_user_1 on widget (user_id);
alter table widget_instance add constraint fk_widget_instance_widget_2 foreign key (widget_id) references widget (id) on delete restrict on update restrict;
create index ix_widget_instance_widget_2 on widget_instance (widget_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists server_node;

drop table if exists user;

drop table if exists widget;

drop table if exists widget_instance;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists server_node_seq;

drop sequence if exists user_seq;

drop sequence if exists widget_seq;

drop sequence if exists widget_instance_seq;

