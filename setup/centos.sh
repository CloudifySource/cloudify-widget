#! /bin/bash
# this script requires 3 more files
# 1. hpcloud.pem - the certificate to connect to the cloud machines
# 2. sysconfig_play - the sysconfig file for the application
# 3. prod.conf - production configuration

export SETUP_WORKDIR=`pwd`
echo "My workdir is $SETUP_WORKDIR"

SYSCONF_FILE=widgetSysconfig

echo "checking if sysconfig file exists"
if [ ! -f ${SYSCONF_FILE} ]; then
    echo "missing ${SYSCONF_FILE}"
    exit 1
fi

cp $SYSCONF_FILE /etc/sysconfig/cloudifyWidget

. /etc/sysconfig/cloudifyWidget

if [ -z "$1" ]; then
    echo "usage centos.sh [ibm|hp]"
    exit 1
fi

TYPE=$1

echo "installing widget for $TYPE"

echo "installing git"
yum  -y install git

mkdir -p $WIDGET_HOME

echo "cloning cloudify-widget"

${WIDGET_HOME}/setup/utils/checkout_project.sh


export UTILS_FOLDER=${WIDGET_HOME}/setup;

source $UTILS_FOLDER/${TYPE}

