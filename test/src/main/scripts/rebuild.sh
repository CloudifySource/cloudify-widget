#!/bin/sh

USERNAME=$1
PASSWORD=$2
TENANT_NAME=$3
ID=$4
IMAGE=$5



#install nova client
#mkdir /export/tgrid/cloudify-widget-test/nova
#cd /export/tgrid/cloudify-widget-test/nova
#wget https://docs.hpcloud.com/file/python-novaclient_2.6.8.tar.gz --no-check-certificate
#tar -zxvf python-novaclient_2.6.8-1hp14.tar.gz
#cd build
#sudo python setup.py install
#cd ..
#wget https://docs.hpcloud.com/file/nova-stuff.tar --no-check-certificate
#tar -xvzf prettytable-0.5.tar.gz
#cd prettytable-0.5
#sudo python setup.py install
#cd ..

export NOVA_USERNAME=$USERNAME
export NOVA_PASSWORD=$PASSWORD
export NOVA_PROJECT_ID=$TENANT_NAME
export NOVA_URL=https://region-a.geo-1.identity.hpcloudsvc.com:35357/v2.0/
export NOVA_VERSION=1.1
export NOVA_REGION_NAME=az-3.region-a.geo-1

echo issuing rebuild command!!!
nova rebuild $ID $IMAGE