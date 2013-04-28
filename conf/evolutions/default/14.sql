create table server_node_event (
  id                        bigint auto_increment not null,
  event_type                varchar(20),
  event_timestamp           bigint,
  msg                       varchar(512),
  server_node_id            bigint,
  constraint pk_server_node_events primary key (id)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
