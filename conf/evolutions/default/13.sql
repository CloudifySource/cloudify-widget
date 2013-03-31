create table widget_icon (
  id                        bigint auto_increment not null,
  data                      longblob,
  name                      varchar(255),
  content_type               varchar(255),
  constraint pk_widget_icon primary key (id))
;


alter table widget add column icon_id bigint;

alter table widget add constraint fk_widget_icon_2 foreign key (icon_id) references widget_icon (id) on delete restrict on update restrict;
create index ix_widget_icon_2 on widget (icon_id);