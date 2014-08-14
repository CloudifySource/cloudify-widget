alter table server_node add column async_bootstrap_start     datetime;
alter table server_node add column async_install_start       datetime;

drop table pool_event_model;