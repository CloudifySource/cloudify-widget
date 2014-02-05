#!/bin/bash

if [ -z "$PLAY_HOME" ]; then
    echo "need to set PLAY_HOME"
    exit 1
fi

cd modules
mvn install
cd ..

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