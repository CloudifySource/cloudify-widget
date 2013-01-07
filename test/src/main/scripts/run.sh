#!/bin/sh
scp -B -i testing.pem -o StrictHostKeyChecking=no test.sh hpcloud.pem centos_auto_setup_machine.sh prod.conf sysconfig_play root@15.185.229.88:/root
ssh -i testing.pem -o StrictHostKeyChecking=no root@15.185.229.88 ./centos_auto_setup_machine.sh && ./test.sh