
CLOUD_NAME=$1



mkdir $CLOUDIFY_HOME/clouds/$CLOUD_NAME/upload/cloudify-overrides/config/
\cp -f $CLOUDIFY_HOME/config/cloudify-webui-context-override.xml $CLOUDIFY_HOME/clouds/$CLOUD_NAME/upload/cloudify-overrides/config/

cd $CLOUDIFY_HOME/bin
./cloudify.sh "bootstrap-cloud --verbose $CLOUD_NAME"