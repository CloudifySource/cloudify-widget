#!/bin/sh
ip=$1

CHECKOUT_FOLDER=cloudify-widget
if [ -z $GIT_LOCATION ] || [ $GIT_LOCATION"xxx" = "xxx" ]; then
    GIT_LOCATION="https://github.com/CloudifySource/cloudify-widget.git"
fi
echo "using git location : ${GIT_LOCATION}"

rm -rf $CHECKOUT_FOLDER
git clone -n --depth 1 $GIT_LOCATION
cd $CHECKOUT_FOLDER
git checkout HEAD upgrade_server.sh
cd -

if [ -z $GIT_BRANCH  ] || [ $GIT_BRANCH"xxx" = "xxx" ]; then
        echo "no branch specified"
else
    echo "checking out branch ${GIT_BRANCH}"
    cd $CHECKOUT_FOLDER
    git checkout origin/$GIT_BRANCH upgrade_server.sh
    cd -
fi

cp $CHECKOUT_FOLDER/upgrade_server.sh upgrade_server.sh
chmod 0755 upgrade_server.sh
rm -rf $CHECKOUT_FOLDER


scp -B -i testing.pem -o StrictHostKeyChecking=no update.sh upgrade_server.sh root@$ip:/root
ssh -i testing.pem -o StrictHostKeyChecking=no root@$ip ./update.sh