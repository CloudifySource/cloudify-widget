#! /bin/bash

########################################
#  This script uses placeholders in the format ##placeholder## that are injected from widget configuration
#
#  The available placeholders are:
#      publicip - the cloudify's public ip. if not given, cloudify will show 127.0.0.1 as public ip as it is localcloud
#      privateip - cloudify's private ip. if not given, cloudify will show 127.0.0.1 as private ip as it is localcloud
#      installNode - if "true" then we install node
#      cloudifyUrl - the URL download cloudify from
#      prebootstrapScript - a string of code to run before bootstrap
#      recipeUrl - a url to a zip file containing a recipe to be installed on bootstrap. skipped if not present.
#      recipeRelativePath - a relative URL inside the zip file to reach the recipe
#      recipeDownloadMethod - wget (default) or s3 (requires more data) to download the recipe's zip file
#
#
###########################################

CLOUDIFY_HOMEDIR_CONF=/etc/cloudify/homedir
CLOUDIFY_FOLDER=`cat $CLOUDIFY_HOMEDIR_CONF`

init(){
    echo "Open firewall ports"
    # iptables -A INPUT -i eth0 -p tcp -m multiport --dports 22,80,443,8080,9000,8100,8099 -m state --state NEW,ESTABLISHED -j ACCEPT

    # iptables -A OUTPUT -o eth0 -p tcp -m multiport --sports 80,443,8080,9000,8100,8099 -m state --state ESTABLISHED -j ACCEPT

    service iptables save
    /etc/init.d/iptables restart

    echo add hostname to /etc/hosts
    echo "127.0.0.1 `hostname`" >> /etc/hosts

    echo Setting sudo privileged mode
    sudo sed -i 's/^Defaults.*requiretty/#&/g' /etc/sudoers
}

install_java(){
    echo "JAVA_HOME is $JAVA_HOME"
    CLOUDIFY_AGENT_ENV_PUBLIC_IP=##publicip##
    CLOUDIFY_AGENT_ENV_PRIVATE_IP=##privateip##
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

}


install_node(){
    INSTALL_NODE="##installNode##"
    if [ "$INSTALL_NODE" = "true" ];then
        echo "installing node"
        yum -y install npm
    else
        echo "not installing node :: [$INSTALL_NODE]"
    fi
}

install_cloudify(){
    echo "installing cloudify"
    CLOUDIFY_URL="##cloudifyUrl##"

    mkdir -p /etc/cloudify
    echo "saving url to file"
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
        echo "saving cloudify home [$CLOUDIFY_FOLDER] to file"
        echo $CLOUDIFY_FOLDER > $CLOUDIFY_HOMEDIR_CONF


        rm -f ~/cloudify.zip

        echo "run prebootstrap script"
        ##prebootstrapScript##

        wget --no-check-certificate "https://raw.github.com/CloudifySource/cloudify-widget/master/conf/cloudify/webui-context.xml"   -O ${CLOUDIFY_FOLDER}/config/cloudify-webui-context-override.xml
        echo Starting Cloudify bootstrap-localcloud `hostname -I`

        # -nic-address `hostname -I`"
    fi
}

bootstrap_localcloud(){
    echo "installing cloudify"

    CLOUDIFY_FOLDER=`cat $CLOUDIFY_HOMEDIR_CONF`

    echo "killing all java and starting cloudify"
    killall -9 java

    ${CLOUDIFY_FOLDER}/bin/cloudify.sh "bootstrap-localcloud"
}


download_recipe_wget(){
    if [ ! -f recipe.zip ]; then
        echo "download recipe using wget method"
        wget -q -O recipe.zip "$RECIPE_URL"

    else
        echo "recipe file already exists, nothing to do"
    fi
}

download_recipe_s3(){
    echo "download recipe using s3 method. getting s3-bash utils"

    S3_APIKEY="##urlAccessKey##"
    S3_SECRETKEY="##urlSecretKey##"
    RECIPE_URL="##recipeUrl##"
    ENDPOINT="##urlEndpoint##"

    mkdir -p /tmp/s3_bash
    wget -O /tmp/s3_bash/s3_bash.zip "https://dl.dropboxusercontent.com/s/bh6zqgci6crp239/s3-bash-master.zip?dl=1&token_hash=AAFDPyOoNEn1JlSipoRXVGSg5INJ7QHfT95hO4WQ1tEnEA&expiry=1401176925"
    cd /tmp/s3_bash
    unzip -o s3_bash.zip
    cd s3-bash-master




    echo -n "$S3_SECRETKEY" > secret

    mkdir -p /tmp/download_recipe
    ./s3-get -k "$S3_APIKEY" -s secret -e $ENDPOINT "$RECIPE_URL" > /tmp/download_recipe/recipe.zip



}

download_recipe() {
    echo "download recipe"
    RECIPE_URL="##recipeUrl##"
    RECIPE_DOWNLOAD_METHOD="##recipeDownloadMethod##"
    mkdir -p /tmp/download_recipe
    cd /tmp/download_recipe

    echo "RECIPE_URL is ($RECIPE_URL)"

    if [ ! -z "$RECIPE_URL" ] && [ "$RECIPE_URL" != "" ];then
        echo "downloading recipe from [$RECIPE_URL] method [$RECIPE_DOWNLOAD_METHOD]"

        if [ "$RECIPE_DOWNLOAD_METHOD" = "s3" ]; then
            download_recipe_s3
        fi

        if [ "$RECIPE_DOWNLOAD_METHOD" = "wget" ]; then
            download_recipe_wget
        fi

    else
        echo "no recipe url and/or no recipe relative path.. not installing recipe"
    fi
}

 install_recipe(){
    RECIPE_RELATIVE_PATH="##recipeRelativePath##"
    echo "RECIPE_RELATIVE_PATH is ($RECIPE_RELATIVE_PATH)"

    if [ -f /tmp/download_recipe/recipe.zip ];then
        echo "found recipe.zip file, installing it"

        cd /tmp/download_recipe
        unzip -o recipe.zip


        echo "going into $RECIPE_RELATIVE_PATH to install"
        cd "$RECIPE_RELATIVE_PATH"
        echo "I am at `pwd` and I am invoking install command on localhost"
        ${CLOUDIFY_FOLDER}/bin/cloudify.sh "connect http://localhost:8100; install-service  -disableSelfHealing --verbose -timeout 200 ."

    else
        echo "not installing recipe [$INSTALL_RECIPE]"
    fi
}

init
install_java
install_node
install_cloudify
bootstrap_localcloud
download_recipe


install_recipe


# cat nohup.out
exit 0