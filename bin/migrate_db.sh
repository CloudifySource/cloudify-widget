# get all sysconf variables we have for play

. /etc/sysconfig/play

UPGRADE_TO=$1

if [ "$UPGRADE_TO" = "create" ];then
    `mysql -u $DB_USER -p$DB_PASSWORD  -e "create database $DB"`
    `mysql -u $DB_USER -p$DB_PASSWORD $DB  < $WIDGET_HOME/conf/evolutions/default/create.sql`
    exit 0
fi

if [ "$UPGRADE_TO" = "version" ];then
    DB_VERSION=`mysql -u $DB_USER -p$DB_PASSWORD $DB -e "select version from patchlevel" --skip-column-names --raw `
    echo "current DB version is $DB_VERSION"
    exit 0
fi


if [ "$UPGRADE_TO" = "" ];then
        echo "ERROR : missing argument version"
        echo "usage db_migrate version"
        exit 1
fi

# echo "upgrading to $UPGRADE_TO"

DB_VERSION=`mysql -u $DB_USER -p$DB_PASSWORD $DB -e "select version from patchlevel" --skip-column-names --raw `
echo "current DB version is $DB_VERSION"
if [ $DB_VERSION -ge $UPGRADE_TO ]; then
        echo "DB version is bigger. will not run migrate scripts"
        exit 0
else
        DB_VERSION=` expr $DB_VERSION + 1 `
        for i in `seq $DB_VERSION $UPGRADE_TO`
        do
                CURR_FILE="$WIDGET_HOME/conf/evolutions/default/$i.sql"
                if [ -f $CURR_FILE ]; then
                        echo "          migrating $CURR_FILE"
                        `mysql -u $DB_USER -p$DB_PASSWORD $DB  < $CURR_FILE`
                        RETVAL=$?
                        if [ "$RETVAL" -gt "0" ]; then
                                echo "failed migrating $i with error $RETVAL"
                                exit 1
                        else
                                `mysql -u $DB_USER -p$DB_PASSWORD $DB -e "update patchlevel set version=$i"`
                        fi
                else
                        echo "missing file $CURR_FILE"
                        exit 1
                fi
        done
fi

echo "done migrating"
