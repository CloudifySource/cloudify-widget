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
cat conf/monit/widget.monit | sed 's/__monit_pidfile__/'"$MONIT_PIDFILE"'/' > /etc/monit.d/widget


# assume there are no conflicts
echo "pulling source from git repository"
git pull
if [ "$?" -ne "0" ]; then
    echo "problems with git pull, run git status to see the problem"
fi

# I know we can commit the files with the correct mode, cannot rely on this in production.
echo "changing mode for sh files"
chmod 755 $WIDGET_HOME/*.sh