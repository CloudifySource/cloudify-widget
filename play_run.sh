#!/bin/bash

if [ -z "$PLAY_HOME" ]; then
    echo "need to set PLAY_HOME"
    exit 1
fi

if [ -z "$MODULES_HOME" ]; then
    echo "MODULES_HOME is undefined. assuming it is at next to PLAY_HOME"
    MODULES_HOME=`pwd`
    MODULES_HOME=`dirname $MODULES_HOME`/cloudify-widget-modules
    echo "settings modules to $MODULES_HOME"
fi



if [ -f $MODULES_HOME/pom.xml ]; then


    mvn install -f $MODULES_HOME/pom.xml
else
    echo "pom.xml was not found under $MODULES_HOME"
    exit 1;
fi

#this file helps run play with personal configuration
#the me.conf file should have as first line:
#import "application.conf" (or some other file)
#then you should start overriding application configuration
#such as DB details and what not.
#the "dev" folder is ignored by GIT, so fear not to commit it by mistake.
#if you want to debug, simply run "play_run.bat debug".

echo "deleting cloudify.widget artifacts from $PLAY_HOME/repository/cache/cloudify.widget/"
rm -Rf $PLAY_HOME/repository/cache/cloudify.widget/


play $* -Dconfig.file=conf/dev/me.conf run