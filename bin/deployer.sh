#! /bin/bash

SERVER_IP=$1
RECIPE_URL=$2
RECIPE_TYPE=$3
cd $CLOUDIFY_HOME/bin

./cloudify.sh "connect http://$SERVER_IP:8100;$RECIPE_TYPE $RECIPE_URL" 

exit 1
