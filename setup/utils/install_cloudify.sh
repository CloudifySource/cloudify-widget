# rerunable script used for installing/updating cloudify

CURRENT_DIRECTORY=`pwd`

cd "$(dirname "$0")"

source load_sysconfig.sh

rm -f /tmp/cloudify.zip


CLOUDIFY_CURRENT_URL=`cat ${WIDGET_HOME}/cloudify-folder/url_version`

echo "comparing [$CLOUDIFY_URL] to [$CLOUDIFY_CURRENT_URL]"
if [ "$CLOUDIFY_URL" = "$CLOUDIFY_CURRENT_URL" ]; then
    echo "same cloudify version, nothing to install"
else
    rm -Rf /usr/lib/cloudify
    echo "wgetting cloudify from $CLOUDIFY_URL"
    wget --no-check-certificate $CLOUDIFY_URL -O /tmp/cloudify.zip

    unzip -q /tmp/cloudify.zip -d /usr/lib/cloudify
    echo "recreating the link"

    GIGASPACES_FOLDER=`ls /usr/lib/cloudify/ | grep giga`

    ln -Tfs /usr/lib/cloudify/$GIGASPACES_FOLDER ${WIDGET_HOME}/cloudify-folder

    echo "saving url to file"
    echo $CLOUDIFY_URL > ${WIDGET_HOME}/cloudify-folder/url_version
    rm -f /tmp/cloudify.zip
fi

echo "overriding webui-context.xml in cloudify installation"
\cp -f ${WIDGET_HOME}/conf/cloudify/webui-context.xml ${WIDGET_HOME}/cloudify-folder/config/cloudify-webui-context-override.xml


echo "upgrading clouds"
\cp -fR ${WIDGET_HOME}/conf/cloudify/clouds/* ${WIDGET_HOME}/cloudify-folder/clouds

cd $CURRENT_DIRECTORY


