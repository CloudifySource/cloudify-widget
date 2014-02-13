#! /bin/bash

echo "JAVA_HOME is $JAVA_HOME"
CLOUDIFY_AGENT_ENV_PUBLIC_IP=##publicip##
CLOUDIFY_AGENT_ENV_PRIVATE_IP=##privateip##

echo Open firewall ports
iptables -A INPUT -i eth0 -p tcp -m multiport --dports 22,80,443,8080,9000,8100,8099 -m state --state NEW,ESTABLISHED -j ACCEPT

iptables -A OUTPUT -o eth0 -p tcp -m multiport --sports 80,443,8080,9000,8100,8099 -m state --state ESTABLISHED -j ACCEPT

service iptables save
/etc/init.d/iptables restart

echo add hostname to /etc/hosts
echo "127.0.0.1 `hostname`" >> /etc/hosts

echo Setting sudo privileged mode
sudo sed -i 's/^Defaults.*requiretty/#&/g' /etc/sudoers


# http://repository.cloudifysource.org/org/cloudifysource/2.7.0-5985-M3/gigaspaces-cloudify-2.7.0-M3-b5985.zip
CLOUDIFY_VERSION=2.7.0
BUILD_NUMBER=5985
MILESTONE=m3
BUILD_TYPE=SNAPSHOT
CLOUDIFY_FOLDER=~/gigaspaces-cloudify-${CLOUDIFY_VERSION}-${MILESTONE}
JAVA_64_URL="http://repository.cloudifysource.org/com/oracle/java/1.6.0_32/jdk-6u32-linux-x64.bin"

MILESTONE_UPPERCASE=`echo $MILESTONE | tr '[:lower:]' '[:upper:]'`
CLOUDIFY_URL="http://repository.cloudifysource.org/org/cloudifysource/${CLOUDIFY_VERSION}-${BUILD_NUMBER}-${MILESTONE_UPPERCASE}/gigaspaces-cloudify-${CLOUDIFY_VERSION}-${MILESTONE}-b${BUILD_NUMBER}.zip"

if [ ! -z "$JAVA_HOME" ]; then
   echo "Java file already exists. not installing java"
   echo "JAVA_HOME is $JAVA_HOME"
else

    echo Downloading JDK from $JAVA_64_URL
    wget -q -O ~/java.bin $JAVA_64_URL
    chmod +x ~/java.bin
    echo -e "\n" > ~/input.txt

    echo Installing JDK
    ./java.bin < ~/input.txt > /dev/null
    rm -f ~/input.txt
    rm -f ~/java.bin

    echo Exporing JAVA_HOME
    # export JAVA_HOME="`pwd`/jdk1.6.0_32"
    # export PATH=$PATH:$JAVA_HOME/bin
    echo "export JAVA_HOME=`pwd`/jdk1.6.0_32" >> ~/.bashrc
    echo 'export PATH=$PATH:$JAVA_HOME/bin' >> ~/.bashrc
    source ~/.bashrc
fi


if [ -f ~/cloudify.zip ];then
    echo "cloudify already downloaded"
else
    echo Downloading cloudify installation from $CLOUDIFY_URL

    wget -q $CLOUDIFY_URL -O ~/cloudify.zip
    if [ $? -ne 0 ]; then
        echo "Failed downloading cloudify installation"
        exit 1
    fi


    echo Unzip cloudify installation
    unzip ~/cloudify.zip > /dev/null

    wget "https://raw.github.com/CloudifySource/cloudify-widget/master/conf/cloudify/webui-context.xml"   -O ${CLOUDIFY_FOLDER}/config/cloudify-webui-context-override.xml
    echo Starting Cloudify bootstrap-localcloud `hostname -I`

    # -nic-address `hostname -I`"
fi

echo "installing node"
yum -y install npm

echo "killing all java and starting cloudify"
killall -9 java
nohup ${CLOUDIFY_FOLDER}/bin/cloudify.sh "bootstrap-localcloud"

echo "installing recipe"
wget http://s3.amazonaws.com/cloudify-widget/2.7/mongodb.zip
unzip mongodb.zip
cd mongodb
${CLOUDIFY_FOLDER}/bin/cloudify.sh "connect http://localhost:8100; install-service ."

cd ..
cat nohup.out
exit 0