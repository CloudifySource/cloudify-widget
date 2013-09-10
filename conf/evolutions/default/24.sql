alter table server_node add column lead_id bigint;
alter table widget_instance drop column lead_id;


alter table server_node add constraint fk_server_node_lead foreign key (lead_id) references lead (id)
on delete restrict on update restrict;
create index ix_server_node_lead on server_node (lead_id);


alter table user add column  permissions_id            bigint;

create table user_permissions (
  id                        bigint auto_increment not null,
  can_assign_leads          tinyint(1) default 0,
  constraint pk_user_permissions primary key (id)) engine=InnoDB default charset=utf8
;

alter table user add constraint fk_user_permissions foreign key (permissions_id) references user_permissions (id) on delete restrict on update restrict;
create index ix_user_permissions on user (permissions_id);


insert into user_permissions (id, can_assign_leads ) select NULL, 0 from user;
update user set  permissions_id = id;


