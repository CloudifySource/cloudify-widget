#!/bin/sh
ip=$1
id=$2

CHECKOUT_FOLDER=cloudify-widget
if [ -z $GIT_LOCATION ] || [ $GIT_LOCATION"xxx" = "xxx" ]; then
    GIT_LOCATION="https://github.com/CloudifySource/cloudify-widget.git"
fi
echo "using git location : ${GIT_LOCATION}"

rm -rf $CHECKOUT_FOLDER
git clone -n --depth 1 $GIT_LOCATION
cd $CHECKOUT_FOLDER
git checkout HEAD centos_auto_setup_machine.sh
cd -

if [ -z $GIT_BRANCH  ] || [ $GIT_BRANCH"xxx" = "xxx" ]; then
        echo "no branch specified"
else
    echo "checking out branch ${GIT_BRANCH}"
    cd $CHECKOUT_FOLDER
    git checkout origin/$GIT_BRANCH centos_auto_setup_machine.sh
    cd -
fi

cp $CHECKOUT_FOLDER/centos_auto_setup_machine.sh centos_auto_setup_machine.sh
chmod 0755 centos_auto_setup_machine.sh
rm -rf $CHECKOUT_FOLDER

scp -B -i testing.pem -o StrictHostKeyChecking=no setup-test.sh run-test.sh hpcloud.pem centos_auto_setup_machine.sh prod.conf sysconfig_play root@$ip:/root
ssh -i testing.pem -o StrictHostKeyChecking=no root@$ip "export GIT_LOCATION=$GIT_LOCATION; export GIT_BRANCH=$GIT_BRANCH; ./centos_auto_setup_machine.sh; ./setup-test.sh $id"
