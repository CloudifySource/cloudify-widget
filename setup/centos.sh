#! /bin/bash
# this script requires 3 more files
# 1. hpcloud.pem - the certificate to connect to the cloud machines
# 2. sysconfig_play - the sysconfig file for the application
# 3. prod.conf - production configuration

if [ ! -f ${SYSCONF_FILE} ]; then
    echo "missing ${SYSCONF_FILE}"
    exit 1
fi


echo "installing git"
yum  -y install git

echo "cloning cloudify-widget"
CHECKOUT_FOLDER=cloudify-widget
if [ -z $GIT_LOCATION ] || [ $GIT_LOCATION"xxx" = "xxx" ]; then
    echo "please define a github repository to checkout from"
    exit 1;
fi
echo "using git location : ${GIT_LOCATION}"

git clone $GIT_LOCATION $CHECKOUT_FOLDER

if [ -z $GIT_BRANCH  ] || [ $GIT_BRANCH"xxx" = "xxx" ]; then
        echo "no branch specified"
else
    echo "checking out branch ${GIT_BRANCH}"
    cd $CHECKOUT_FOLDER
    git checkout $GIT_BRANCH
    cd -
fi


$UTILS_FOLDER=