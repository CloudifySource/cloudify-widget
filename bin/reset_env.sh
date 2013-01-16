. /etc/sysconfig/play

mysqldump -u $DB_ADMIN -p$DB_ADMIN_PASSWORD --add-drop-table --no-data --database $DB --no-data > tmp_schema_dump.sql
mysql -u $DB_ADMIN -p$DB_ADMIN_PASSWORD < tmp_schema_dump

rm -f tmp_schema_dump.sql