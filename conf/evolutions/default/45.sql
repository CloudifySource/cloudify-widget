drop table create_machine_output;

create table create_machine_output (
  id                        bigint auto_increment not null,
  max_tries                 integer,
  current_try               integer,
  created                   bigint,
  exit_code                 integer,
  output                    longtext,
  exception                 longtext,
  output_read               tinyint(1) default 0,
  constraint pk_create_machine_output primary key (id))
;