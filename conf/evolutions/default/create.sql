CREATE TABLE `patchlevel` (
      `version` bigint(20) NOT NULL DEFAULT '0',
      PRIMARY KEY (`version`)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into patchlevel values (0);