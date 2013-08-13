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


# 2.7.0-SNAPSHOT/gigaspaces-cloudify-2.7.0-m1-b5982-4.zip

CLOUDIFY_VERSION=2.7.0
BUILD_NUMBER=b5982-4
MILESTONE=m1
BUILD_TYPE=SNAPSHOT

BUILD_VERSION=${CLOUDIFY_VERSION}-${BUILD_TYPE}
CLOUDIFY_MILESTONE=${CLOUDIFY_VERSION}-${MILESTONE}
CLOUDIFY_BUILD=${CLOUDIFY_VERSION}-${MILESTONE}-${BUILD_NUMBER}
echo "upgrading cloudify to version $CLOUDIFY_VERSION $CLOUDIFY_BUILD"
CLOUDIFY_FOLDER=gigaspaces-cloudify-${CLOUDIFY_VERSION}-${MILESTONE}
CLOUDIFY_ZIP_NAME=${CLOUDIFY_FOLDER}-${BUILD_NUMBER}
CLOUDIFY_FILE=${CLOUDIFY_ZIP_NAME}.zip
echo searching if file [${CLOUDIFY_FILE}] already exists
if [ -f /root/$CLOUDIFY_FILE ]; then
    echo "cloudify already installed, nothing to go"
else
    echo "executing wget on [http://repository.cloudifysource.org/org/cloudifysource/${BUILD_VERSION}/${CLOUDIFY_FILE}]"
    wget "http://repository.cloudifysource.org/org/cloudifysource/${BUILD_VERSION}/${CLOUDIFY_FILE}" -O /root/$CLOUDIFY_FILE
    if [ $? -ne 0 ]; then
        echo "wget failed. deleting the file"
        rm -f  /root/$CLOUDIFY_FILE
    else
        unzip /root/$CLOUDIFY_FILE -d /root/
        ln -Tfs /root/${CLOUDIFY_FOLDER} cloudify-folder
    fi
fi


echo "overriding webui-context.xml in cloudify installation"
\cp -f conf/cloudify/webui-context.xml cloudify-folder/config/cloudify-webui-context-override.xml

echo "injecting variables to cloudify.conf, and generating cloudify-prod.conf - extend this file in production instead of cloudify.conf"
cat conf/cloudify.conf | sed 's,__CLOUDIFY_SECURITY_GROUP__,'"$CLOUDIFY_SECURITY_GROUP"','  > conf/cloudify-prod.conf



TMP_SITE_CONF=conf/nginx/output.nginx
SITE_CONF_TARGET=/etc/nginx/sites-available/$SITE_DOMAIN
NGINX_CONF_SRC=conf/nginx/nginx.conf
NGINX_CONF_TARGET=/etc/nginx/nginx.conf
echo "copying nginx configurations"

cmp  -s ${NGINX_CONF_SRC} ${NGINX_CONF_TARGET}
if [ $? -ne 0 ]; then
    \cp -f ${NGINX_CONF_SRC} ${NGINX_CONF_TARGET}
    service nginx restart
else
     echo "nginx configuration did not change, not copying"
fi

cat conf/nginx/site.nginx  | sed 's/__domain_name__/'"$SITE_DOMAIN"'/' | sed 's/__staging_name__/'"$SITE_STAGING_DOMAIN"'/' > ${TMP_SITE_CONF}
cmp  -s ${TMP_SITE_CONF}  ${SITE_CONF_TARGET}
if [ $? -eq 1 ]; then
    \cp -f ${TMP_SITE_CONF} ${SITE_CONF_TARGET}
    echo "restarting nginx"
    service nginx restart
else
    echo "nginx configuration did not change, not restarting"
fi

\rm -f ${TMP_SITE_CONF}

# assume with are in "cloudify-widget" folder
echo "copying error pages"
# copy content from public error_pages to that path
\cp -Rvf public/error_pages /var/www/cloudifyWidget/public

echo "upgrading DB schema"
#find which is the latest version of DB
# ll all the files, remove "create" script, remove extension, sort in descending order and output first line.
db_version=`ls conf/evolutions/default -1 | grep -v create |  sed -e 's/\.[a-zA-Z]*$//' | sort -r -n | head -1`
bin/migrate_db.sh $db_version

echo "upgrading init.d script"
\cp -f conf/initd/widget /etc/init.d/widget
chmod 755 /etc/init.d/widget


echo "upgrading hp-cloud templates"
\cp -f conf/cloudify/hp-cloud.groovy cloudify-folder/clouds/hp/hp-cloud.groovy
\cp -f conf/cloudify/hp-cloud.properties cloudify-folder/clouds/hp/hp-cloud.properties


echo "upgrading monit configurations"
cat conf/monit/conf.monit | sed 's,__monit_from__,'"$MONIT_FROM"',' | sed 's,__monit_to__,'"$MONIT_SET_ALERT"',' > /etc/monit.conf
\cp -f conf/monit/mysql.monit /etc/monit.d/mysqld
MONIT_PIDFILE=$WIDGET_HOME/RUNNING_PID
cat conf/monit/widget.monit | sed 's,__monit_pidfile__,'"$MONIT_PIDFILE"',' > /etc/monit.d/widget


