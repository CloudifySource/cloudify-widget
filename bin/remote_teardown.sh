
cd $CLOUDIFY_HOME/bin
./cloudify.sh "connect http://$IP; teardown --force --verbose -timeout 120 "bootstrap-cloud -timeout 120 --verbose $CLOUD_NAME"