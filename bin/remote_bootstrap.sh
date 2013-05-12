
CLOUD_NAME=$1

cd $CLOUDIFY_HOME/bin

mkdir cloudify-folder/clouds/$CLOUD_NAME/upload/cloudify-overrides/config/
\cp -f cloudify-folder/config/cloudify-webui-context-override.xml cloudify-folder/clouds/$CLOUD_NAME/upload/cloudify-overrides/config/

./cloudify.sh "bootstrap-cloud --verbose $CLOUD_NAME"