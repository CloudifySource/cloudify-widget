
CLOUD_NAME=$1

cd $CLOUDIFY_HOME/bin

./cloudify.sh "bootstrap-cloud --verbose $CLOUD_NAME"