execute(){
    echo "executing $1"
    source ${WIDGET_HOME}/setup/utils/$1.sh
}

execute update_cloudify_widget

execute update_cloudify_widget_modules

# note: this script does not need an update. it is edited on production and never committed to CVS.
. /etc/sysconfig/play


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



