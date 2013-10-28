alter table server_node change creation_time busy_since bigint(20);
alter table server_node drop column busy;