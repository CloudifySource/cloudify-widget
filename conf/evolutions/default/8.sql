-- add recipe type
alter table widget_instance add column recipe_type  varchar(20);

-- add widget_instance_id - FK to widget_instance
alter table server_node add column   widget_instance_id bigint;

-- migrate data from widget_instance.server_id to server_node.widget_instance_id
UPDATE server_node AS sn
LEFT JOIN widget_instance as wi
ON wi.instance_id = sn.server_id
SET sn.widget_instance_id = wi.id;

-- remove redundant columns
alter table widget_instance drop column instance_id;
alter table widget_instance drop column anonymouse;
alter table widget_instance drop column public_ip;
