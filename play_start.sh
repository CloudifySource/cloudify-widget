#! /usr/bin/env sh

#if [ -f /var/lib/takipi/so/libTakipiAgent.so ]; then
#    echo "adding takipi to java options"
#    export _JAVA_OPTIONS="${_JAVA_OPTIONS} -agentpath:/var/lib/takipi/so/libTakipiAgent.so"
#fi
nohup play -Dconfig.file=conf/dev/me.conf start &> /var/log/cloudifyWidget/nohup.out