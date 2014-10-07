create table create_machine_output (
  id                        bigint auto_increment not null,
  content                   longtext,
  created                   bigint,
  output_read                tinyint(1) default 0,
  constraint pk_create_machine_output primary key (id))
;
