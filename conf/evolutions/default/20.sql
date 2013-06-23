create table pool_event_model (
  id                        bigint auto_increment not null,
  timestamp                 bigint,
  last_update               bigint,
  event                     longtext,
  empty                     tinyint(1) default 0,
  version                   bigint not null,
  constraint pk_pool_event_model primary key (id))  ENGINE=InnoDB DEFAULT CHARSET=utf8
;