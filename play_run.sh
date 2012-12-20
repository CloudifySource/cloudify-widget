#!/bin/bash

#this file helps run play with personal configuration
#the me.conf file should have as first line:
#import "application.conf" (or some other file)
#then you should start overriding application configuration
#such as DB details and what not.
#the "dev" folder is ignored by GIT, so fear not to commit it by mistake.
#if you want to debug, simply run "play_run.bat debug".

play $* -Dconfig.file=conf/dev/me.conf run