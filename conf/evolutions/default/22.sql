alter table lead add column confirmation_code varchar(255) default NULL;

alter table lead add constraint uq_owner_email unique (owner_id,email);