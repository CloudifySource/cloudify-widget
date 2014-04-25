#! /bin/bash -x

yum install -y -q ntp
service ntpd restart
sleep 60s