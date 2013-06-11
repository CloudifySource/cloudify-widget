#! /bin/bash

SERVER_IP=$1
INSTALL_NAME=$2
echo "ignore: CLOUDIFY_HOME is ${CLOUDIFY_HOME}"
cd $CLOUDIFY_HOME/bin

./cloudify.sh "connect http://$SERVER_IP:8100; use-application default; uninstall-service $INSTALL_NAME"

exit 1
