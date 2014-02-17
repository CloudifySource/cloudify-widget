# note: sysconfig script does not need an update. it is edited on production and never committed to CVS.
source load_sysconfig.sh


execute(){
    echo "executing $1"
    source ${WIDGET_HOME}/setup/utils/$1.sh
}

execute update_me_conf

execute update_sysconf

execute update_cloudify_widget


# I know we can commit the files with the correct mode, cannot rely on this in production.
echo "changing mode for sh files"
chmod 755 $WIDGET_HOME/*.sh
chmod 755 $WIDGET_HOME/bin/*.sh


execute install_cloudify

execute update_nginx_configuration

execute update_error_pages

execute update_db_schema

execute update_service_initd

execute update_monit

execute update_logrotate_conf



