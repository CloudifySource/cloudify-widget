#!/bin/sh
ip=$1
USERNAME=$2
PASSWORD=$3
TENANT_NAME=$4
ID=$5

SMTPHOST=$6
SMTPUSER=$7
SMTPPASS=$8
RECIPIENTS=$9

scp -B -i testing.pem -o StrictHostKeyChecking=no setup-test.sh run-test.sh hpcloud.pem centos_auto_setup_machine.sh prod.conf sysconfig_play root@$ip:/root

ssh -i testing.pem -o StrictHostKeyChecking=no root@$ip ./run-test.sh $USERNAME $PASSWORD $TENANT_NAME $ID $SMTPHOST $SMTPUSER $SMTPPASS $RECIPIENTS