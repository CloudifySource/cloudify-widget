#! /bin/bash

echo "JAVA_HOME is $JAVA_HOME"
CLOUDIFY_AGENT_ENV_PUBLIC_IP=##publicip##
CLOUDIFY_AGENT_ENV_PRIVATE_IP=##privateip##

echo Open firewall ports
# iptables -A INPUT -i eth0 -p tcp -m multiport --dports 22,80,443,8080,9000,8100,8099 -m state --state NEW,ESTABLISHED -j ACCEPT

# iptables -A OUTPUT -o eth0 -p tcp -m multiport --sports 80,443,8080,9000,8100,8099 -m state --state ESTABLISHED -j ACCEPT

service iptables save
/etc/init.d/iptables restart

echo add hostname to /etc/hosts
echo "127.0.0.1 `hostname`" >> /etc/hosts

echo Setting sudo privileged mode
sudo sed -i 's/^Defaults.*requiretty/#&/g' /etc/sudoers


# http://repository.cloudifysource.org/org/cloudifysource/2.7.0-5985-M3/gigaspaces-cloudify-2.7.0-M3-b5985.zip
JAVA_64_URL="http://repository.cloudifysource.org/com/oracle/java/1.6.0_32/jdk-6u32-linux-x64.bin"



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

echo "installing node"
yum -y install npm

echo "installing cloudify"
CLOUDIFY_URL="##cloudifyUrl##"
CLOUDIFY_HOMEDIR_CONF=/etc/cloudify/homedir
mkdir -p /etc/cloudify
echo $CLOUDIFY_URL > /etc/cloudify/url
if [ -f $CLOUDIFY_HOMEDIR_CONF ];then
    echo "cloudify already downloaded and is at `cat $CLOUDIFY_HOMEDIR_CONF`"
else
    echo Downloading cloudify installation from $CLOUDIFY_URL

    wget --no-check-certificate "$CLOUDIFY_URL" -O ~/cloudify.zip
    if [ $? -ne 0 ]; then
        echo "Failed downloading cloudify installation"
        exit 1
    fi

    unzip ~/cloudify.zip > /dev/null

    CLOUDIFY_FOLDER="`pwd`/`ls ~ | grep giga`"
    echo $CLOUDIFY_FOLDER > $CLOUDIFY_HOMEDIR_CONF

    echo "saving url to file"


    rm -f ~/cloudify.zip

    echo "run prebootstrap script"
    ##prebootstrapScript##

    wget "https://raw.github.com/CloudifySource/cloudify-widget/master/conf/cloudify/webui-context.xml"   -O ${CLOUDIFY_FOLDER}/config/cloudify-webui-context-override.xml
    echo Starting Cloudify bootstrap-localcloud `hostname -I`

    # -nic-address `hostname -I`"
fi

CLOUDIFY_FOLDER=`cat $CLOUDIFY_HOMEDIR_CONF`
echo "killing all java and starting cloudify"
killall -9 java
# nohup ${CLOUDIFY_FOLDER}/bin/cloudify.sh "bootstrap-localcloud"
${CLOUDIFY_FOLDER}/bin/cloudify.sh "bootstrap-localcloud"

echo "installing recipe"
RECIPE_URL="##recipeUrl##"
RECIPE_RELATIVE_PATH="##recipeRelativePath##"

echo "RECIPE_URL is ($RECIPE_URL)"
echo "RECIPE_RELATIVE_PATH is ($RECIPE_RELATIVE_PATH)"


if [ ! -z "$RECIPE_URL" ] &&  [ ! -z "$RECIPE_RELATIVE_PATH" ] && [ "$RECIPE_URL" != "" ] && [ "$RECIPE_RELATIVE_PATH" != "" ];then

    if [ ! -f recipe.zip ]; then
        echo "downloading recipe from $RECIPE_URL"
        wget -q -O recipe.zip "$RECIPE_URL"
        unzip recipe.zip
    fi
    CURRENT_DIR=`pwd`
    echo "going into $RECIPE_RELATIVE_PATH to install"
    cd "$RECIPE_RELATIVE_PATH"
    echo "I am at `pwd` and I am invoking install command on localhost"
    ${CLOUDIFY_FOLDER}/bin/cloudify.sh "connect http://localhost:8100; install-service --verbose -timeout 200 ."
    cd ${CURRENT_DIR}
else
    echo "no recipe url and/or no recipe relative path.. not installing recipe"
fi

# cat nohup.out
exit 0