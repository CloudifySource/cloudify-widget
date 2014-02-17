CURRENT_DIRECTORY=`pwd`

cd "$(dirname "$0")"

source load_sysconfig.sh
echo "creating DB scheme"
${WIDGET_HOME}/bin/migrate_db.sh create

cd $CURRENT_DIRECTORY
