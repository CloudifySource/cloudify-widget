
CLOUD_NAME=$1

GS_HOME=~/gigaspaces-cloudify-2.3.0-ga

cd $GS_HOME/bin

./cloudify.sh "bootstrap-cloud --verbose $CLOUD_NAME"