#! /usr/bin/env sh

if [ -f /var/lib/takipi/so/libTakipiAgent.so ]; then
    _JAVA_OPTIONS=" ${_JAVA_OPTIONS} -agentpath:/var/lib/takipi/so/libTakipiAgent.so"
fi
nohup play -Dconfig.file=conf/prod.conf start &