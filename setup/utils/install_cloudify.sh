
# http://repository.cloudifysource.org/org/cloudifysource/2.7.0-5985-M3/gigaspaces-cloudify-2.7.0-M3-b5985.zip

echo "CLOUDIFY_UPGRADE="$CLOUDIFY_UPGRADE
rm -f /tmp/cloudify.zip

if [ "$CLOUDIFY_UPGRADE" != "MANUAL" ] || [ ! -f ${WIDGET_HOME}/cloudify-folder ]; then

    CLOUDIFY_URL=https://s3-eu-west-1.amazonaws.com/gigaspaces-repository-eu/org/cloudifysource/2.7.0-5996-RELEASE/gigaspaces-cloudify-2.7.0-ga-b5996.zip

    $CLOUDIFY_CURRENT_URL=`cat /usr/lib/cloudify/url_version`

    echo "comparing $CLOUDIFY_URL to $CLOUDIFY_CURRENT_URL"
    if [ "$CLOUDIFY_URL" = "$CLOUDIFY_CURRENT_URL" ]; then
        echo "same cloudify version, nothing to install"
    else
        rm -Rf /usr/lib/cloudify
        echo "wgetting cloudify from $CLOUDIFY_URL"
        wget --no-check-certificate $CLOUDIFY_URL -O /tmp/cloudify.zip

        unzip /tmp/cloudify.zip -d /usr/lib/cloudify
        echo "recreating the link"

        GIGASPACES_FOLDER=`ls /usr/lib/cloudify/ | grep giga`

        ln -Tfs /usr/lib/cloudify/$GIGASPACES_FOLDER ${WIDGET_HOME}/cloudify-folder

        echo "saving url to file"
        echo $CLOUDIFY_URL > ${WIDGET_HOME}/cloudify-folder/url_version
        rm -f /tmp/cloudify.zip
    fi

    echo "overriding webui-context.xml in cloudify installation"
    \cp -f ${WIDGET_HOME}/conf/cloudify/webui-context.xml ${WIDGET_HOME}/cloudify-folder/config/cloudify-webui-context-override.xml

    echo "injecting variables to cloudify.conf, and generating cloudify-prod.conf - extend this file in production instead of cloudify.conf"
    cat ${WIDGET_HOME}/conf/cloudify.conf | sed 's,__CLOUDIFY_SECURITY_GROUP__,'"$CLOUDIFY_SECURITY_GROUP"','  > ${WIDGET_HOME}/conf/cloudify-prod.conf


fi

echo "upgrading hp-cloud templates"
\cp -f ${WIDGET_HOME}/conf/cloudify/hp-cloud.groovy ${WIDGET_HOME}/cloudify-folder/clouds/hp/hp-cloud.groovy
\cp -f ${WIDGET_HOME}/conf/cloudify/hp-cloud.properties ${WIDGET_HOME}/cloudify-folder/clouds/hp/hp-cloud.properties
\cp -f ${WIDGET_HOME}/conf/cloudify/prebootstrap ${WIDGET_HOME}/cloudify-folder/clouds/hp/uploads/pre-bootstrap.sh

\cp -f ${WIDGET_HOME}/conf/cloudify/softlayer-cloud.groovy ${WIDGET_HOME}/cloudify-folder/clouds/softlayer/softlayer-cloud.groovy
\cp -f ${WIDGET_HOME}/conf/cloudify/softlayer-cloud.properties ${WIDGET_HOME}/cloudify-folder/clouds/softlayer/softlayer-cloud.properties
\cp -f ${WIDGET_HOME}/conf/cloudify/prebootstrap ${WIDGET_HOME}/cloudify-folder/clouds/softlayer/uploads/pre-bootstrap.sh



