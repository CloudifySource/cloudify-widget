# assume there are no conflicts
echo "pulling source from git repository"
git pull
if [ "$?" -ne "0" ]; then       # we need to consider using hard reset here instead of specifying there's a problem: git reset --hard
    echo "problems with git pull, run git status to see the problem"
    exit 1
fi

# note: this script does not need an update. it is edited on production and never committed to CVS.
. /etc/sysconfig/play


# I know we can commit the files with the correct mode, cannot rely on this in production.
echo "changing mode for sh files"
chmod 755 $WIDGET_HOME/*.sh
chmod 755 $WIDGET_HOME/bin/*.sh


echo "copying nginx configurations"
\cp -f conf/nginx/nginx.conf /etc/nginx/
cat conf/nginx/site.nginx  | sed 's/__domain_name__/'"$SITE_DOMAIN"'/' | sed 's/__staging_name__/'"$SITE_STAGING_DOMAIN"'/' > /etc/nginx/sites-available/$SITE_DOMAIN
echo "restarting nginx"
service nginx restart

# assume with are in "cloudify-widget" folder
echo "copying error pages"
# copy content from public error_pages to that path
\cp -Rvf public/error_pages /var/www/cloudifyWidget/public

echo "upgrading DB schema"
#find which is the latest version of DB
# ll all the files, remove "create" script, remove extension, sort in descending order and output first line.
db_version=`ls conf/evolutions/default -1 | grep -v create |  sed -e 's/\.[a-zA-Z]*$//' | sort -r | head -1`
bin/migrate_db.sh $db_version

echo "upgrading init.d script"
\cp -f conf/initd/widget /etc/init.d/widget
chmod 755 /etc/init.d/widget

echo "upgrading monit configurations"
cat conf/monit/conf.monit | sed 's,__monit_from__,'"$MONIT_FROM"',' | sed 's,__monit_to__,'"$MONIT_SET_ALERT"',' > /etc/monit.conf
\cp -f conf/monit/mysql.monit /etc/monit.d/mysqld
MONIT_PIDFILE=$WIDGET_HOME/RUNNING_PID
cat conf/monit/widget.monit | sed 's,__monit_pidfile__,'"$MONIT_PIDFILE"',' > /etc/monit.d/widget
