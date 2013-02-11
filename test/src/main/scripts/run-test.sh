#!/bin/sh
USERNAME=$1
PASSWORD=$2
TENANT_NAME=$3
ID=$4

SMTPHOST=$5
SMTPUSER=$6
SMTPPASS=$7
RECIPIENTS=$8


export DISPLAY=:1

cd /root/play-2.0.4/cloudify-widget
./play_start.sh

url=http://localhost:9000
#[ !wget $url 2>&1 | grep --quiet "200 OK" ] &&
NEXT_WAIT_TIME=0
TIMEOUT=600
while [ $NEXT_WAIT_TIME != $TIMEOUT ] && ! wget $url 2>&1 | grep --quiet "200 OK" ;
do
 sleep 1
 NEXT_WAIT_TIME=`expr $NEXT_WAIT_TIME + 1`
 echo waiting for site...
done;

if [ $NEXT_WAIT_TIME == $TIMEOUT ];
then
  echo couldn\`t reach site!!!
  exit 1
fi;
	
#start the tests
cd /root/play-2.0.4/cloudify-widget/test
mvn test -U -Didentity=$TENANT_NAME:$USERNAME -Dcredential=$PASSWORD -Did=$ID -Dlocation=az-3.region-a.geo-1
STATUS=$?
mvn surefire-report:report-only -q
cd -
play stop
killall -9 java

cd /root/play-2.0.4/cloudify-widget/test
mvn exec:java -Dexec.mainClass="org.cloudifysource.widget.test.MailSender" -Dexec.args="${SMTPHOST} ${SMTPUSER} ${SMTPPASS} ${RECIPIENTS}"

exit $STATUS