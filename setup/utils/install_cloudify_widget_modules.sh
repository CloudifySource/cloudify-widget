${WIDGET_HOME}/setup/utils/checkout_project.sh $MODULES_GIT_LOCATION $MODULES_GIT_BRANCH $MODULES_HOME
CURRENT_DIR=`pwd`
cd ${MODULES_HOME}/bin
./maven-install-custom.sh
cd $CURRENT_DIR

