alter table widget add column require_login tinyint(1) default 0;
alter table widget add column login_verification_url varchar(255);
alter table widget add column web_service_key varchar(255);