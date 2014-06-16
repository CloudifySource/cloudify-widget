CLOUDIFY_HOMEDIR_CONF=/etc/cloudify/homedir
CLOUDIFY_FOLDER=`cat $CLOUDIFY_HOMEDIR_CONF`


${CLOUDIFY_FOLDER}/bin/cloudify.sh "teardown-localcloud"