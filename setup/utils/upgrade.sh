# assume there are no conflicts
echo "pulling source from git repository"
git pull
if [ "$?" -ne "0" ]; then       # we need to consider using hard reset here instead of specifying there's a problem: git reset --hard
    echo "problems with git pull, run git status to see the problem"
    exit 1
fi

git log --oneline --abbrev=30 ORIG_HEAD.. >> automatic_changelog

# note: this script does not need an update. it is edited on production and never committed to CVS.
. /etc/sysconfig/play


# I know we can commit the files with the correct mode, cannot rely on this in production.
echo "changing mode for sh files"
chmod 755 $WIDGET_HOME/*.sh
chmod 755 $WIDGET_HOME/bin/*.sh

# http://repository.cloudifysource.org/org/cloudifysource/2.7.0-5985-M3/gigaspaces-cloudify-2.7.0-M3-b5985.zip

source ${WIDGET_HOME}/setup/utils/install_cloudify.sh

source ${WIDGET_HOME}/setup/utils/update_nginx_configuration.sh

source ${WIDGET_HOME}/setup/utils/update_error_pages.sh

source ${WIDGET_HOME}/setup/utils/update_db_schema.sh

source ${WIDGET_HOME}/setup/utils/update_service_initd.sh


echo "upgrading monit configurations"
cat conf/monit/conf.monit | sed 's,__monit_from__,'"$MONIT_FROM"',' | sed 's,__monit_to__,'"$MONIT_SET_ALERT"',' > /etc/monit.conf
\cp -f conf/monit/mysql.monit /etc/monit.d/mysqld
MONIT_PIDFILE=$WIDGET_HOME/RUNNING_PID
cat conf/monit/widget.monit | sed 's,__monit_pidfile__,'"$MONIT_PIDFILE"',' > /etc/monit.d/widget


