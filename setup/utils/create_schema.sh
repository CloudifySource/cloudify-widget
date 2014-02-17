source load_sysconfig.sh
echo "creating DB scheme"
${WIDGET_HOME}/bin/migrate_db.sh create
