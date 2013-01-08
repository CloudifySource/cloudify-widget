#!/bin/sh
# install maven
id=$1


url=http://localhost:9000
while ! wget --spider $url 2>&1 | grep --quiet "200 OK"; do sleep 1; echo waiting for site...; done;
	
#start the tests
cd /root/play-2.0.4/cloudify-widget/test
mvn test -U -X