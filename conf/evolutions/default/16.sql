alter table server_node change user_name project varchar (255) ;
alter table server_node change api_key api_secret_key varchar (255) ;
alter table server_node add column api_key varchar(255) after project;

alter table widget add column console_url_service varchar(255);
alter table widget_instance add column service_public_ip varchar(50);
