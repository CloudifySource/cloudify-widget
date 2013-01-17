#!/bin/sh
ip=$1
id=$2
scp -B -i testing.pem -o StrictHostKeyChecking=no setup-test.sh run-test.sh hpcloud.pem centos_auto_setup_machine.sh prod.conf sysconfig_play root@$ip:/root
ssh -i testing.pem -o StrictHostKeyChecking=no root@$ip "./centos_auto_setup_machine.sh; ./setup-test.sh $id"