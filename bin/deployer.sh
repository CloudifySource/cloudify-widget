#! /bin/bash

SERVER_IP=$1
RECIPE_URL=$2
RECIPE_TYPE=$3
GS_HOME=~/gigaspaces-cloudify-2.3.0-ga

cd $GS_HOME/bin

./cloudify.sh "connect http://$SERVER_IP:8100;$RECIPE_TYPE $RECIPE_URL" 

exit 1
