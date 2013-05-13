alter table server_node drop column widget_instance_id;

alter table widget_instance add column   server_node_id bigint;