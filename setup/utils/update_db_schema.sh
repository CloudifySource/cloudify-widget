echo "upgrading DB schema"
#find which is the latest version of DB
# ll all the files, remove "create" script, remove extension, sort in descending order and output first line.
source load_sysconfig.sh
db_version=`ls ${WIDGET_HOME}/conf/evolutions/default -1 | grep -v create |  sed -e 's/\.[a-zA-Z]*$//' | sort -r -n | head -1`
call ${WIDGET_HOME}/bin/migrate_db.sh $db_version
