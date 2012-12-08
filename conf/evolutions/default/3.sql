create table patchlevel (
  version bigint,
  primary key (`version`)
);

insert into patchlevel values (2);

alter table user            change column `id` `id`  bigint not null auto_increment;
alter table server_node     change column `id` `id`  bigint not null auto_increment;
alter table widget_instance change column `id` `id`  bigint not null auto_increment;


alter table widget drop foreign key  `fk_widget_user_1` ;
alter table widget drop key `ix_widget_user_1`;
alter table widget_instance drop foreign key `fk_widget_instance_widget_2`;

alter table widget          change column `id` `id`  bigint not null auto_increment;
 alter table widget_instance change column `widget_id` `widget_id` bigint;

 alter table widget add KEY `ix_widget_user_1` (`user_id`);
 alter table widget add CONSTRAINT `fk_widget_user_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
 alter table widget_instance add CONSTRAINT `fk_widget_instance_widget_2` FOREIGN KEY (`widget_id`) REFERENCES `widget` (`id`);