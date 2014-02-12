#! /bin/bash
# this script requires 3 more files
# 1. hpcloud.pem - the certificate to connect to the cloud machines
# 2. sysconfig_play - the sysconfig file for the application
# 3. prod.conf - production configuration


PROD_CONF_FILE=~/prod.conf
PEM_FILE=~/hpcloud.pem
SYSCONF_FILE=~/sysconfig_play

echo "checking conf file missing"
utils/check_file_missing.sh ${PROD_CONF_FILE}
echo "checking pem file missing"
utils/check_file_missing.sh ${PEM_FILE}
echo "checking sysconf is missing"
utils/check_file_missing.sh ${SYSCONF_FILE}

# assuming there is a prod.conf copied to here
echo "copying configuration files"
\cp -f ${PROD_CONF_FILE} cloudify-widget/conf
\cp -f ${PEM_FILE} cloudify-widget/bin
ln -fs ~/${CLOUDIFY_FOLDER} cloudify-widget/cloudify-folder # create a symbolic link to cloudify home.
chmod 755 cloudify-widget/*.sh
chmod 755 cloudify-widget/bin/*.sh

cd cloudify-widget



### actual copy of files is done in "upgrade" script



echo "creating error pages"
# create path /var/www/cloudifyWidget/public/error_pages
mkdir -p /var/www/cloudifyWidget/public/error_pages


echo "upgrading system"
./upgrade_server.sh








