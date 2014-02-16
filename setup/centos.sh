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

cp /etc/sysconfig/cloudifyWidget

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

if [ -z $GIT_LOCATION ] || [ $GIT_LOCATION"xxx" = "xxx" ]; then
    echo "please define a github repository to checkout from"
    exit 1;
fi
echo "using git location : ${GIT_LOCATION}"

if [ ! -f $WIDGET_HOME ]; then

    if [ -f $WIDGET_HOME/.widgetGitLocation ]; then
        LAST_GIT_LOCATION=`cat $WIDGET_HOME/.widgetGitLocation`

        if [ "$LAST_GIT_LOCATION" != "$GIT_LOCATION" ]; then
            echo "Last git location does not match"
            exit 1
        fi
    fi

    echo "cloning git repository from $GIT_LOCATION"
    git clone $GIT_LOCATION $WIDGET_HOME

    if [ -z $GIT_BRANCH  ] || [ $GIT_BRANCH"xxx" = "xxx" ]; then
            echo "no branch specified"
    else
        echo "checking out branch ${GIT_BRANCH}"
        cd $WIDGET_HOME
        git checkout $GIT_BRANCH
        cd -
    fi

    echo $GIT_LOCATION > ${WIDGET_HOME}/.widgetGitLocation

else
    echo "already checked out"
fi


export UTILS_FOLDER=${WIDGET_HOME}/setup;

source $UTILS_FOLDER/${TYPE}

