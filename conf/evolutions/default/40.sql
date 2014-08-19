alter table widget add column auto_refresh_recipe   tinyint(1) default 1;
alter table widget add column auto_refresh_provider   tinyint(1) default 1;
alter table widget add column cloud_provider_url   varchar(511);
alter table widget add column cloud_provider_root_dir   varchar(255);