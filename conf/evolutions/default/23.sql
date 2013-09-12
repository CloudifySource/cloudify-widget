alter table widget_instance add column lead_id bigint;

alter table server_node add column creation_time bigint;

alter table server_node drop column expiration_time;