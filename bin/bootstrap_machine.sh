#! /bin/bash

echo Open firewall ports
iptables -A INPUT -i eth0 -p tcp -m multiport --dports 22,80,443,8080,9000,8100,8099 -m state --state NEW,ESTABLISHED -j ACCEPT

iptables -A OUTPUT -o eth0 -p tcp -m multiport --sports 80,443,8080,9000,8100,8099 -m state --state ESTABLISHED -j ACCEPT

service iptables save
/etc/init.d/iptables restart

echo add hostname to /etc/hosts
echo "127.0.0.1 `hostname`" >> /etc/hosts

JAVA_64_URL="http://repository.cloudifysource.org/com/oracle/java/1.6.0_32/jdk-6u32-linux-x64.bin"
CLOUDIFY_URL="http://repository.cloudifysource.org/org/cloudifysource/2.2.0-RELEASE/gigaspaces-cloudify-2.2.0-ga-b2500.zip"

echo Downloading JDK from $JAVA_64_URL
wget -q -O ~/java.bin $JAVA_64_URL
chmod +x ~/java.bin
echo -e "\n" > ~/input.txt

echo Installing JDK
./java.bin < ~/input.txt > /dev/null
rm -f ~/input.txt
rm ~/java.bin

echo Exporing JAVA_HOME
export JAVA_HOME="`pwd`/jdk1.6.0_32"
echo "export JAVA_HOME=$JAVA_HOME" >> ~/.bashrc
echo 'export PATH=$PATH:$JAVA_HOME/bin' >> ~/.bashrc

echo Downloading cloudify installation from $CLOUDIFY_URL
wget -q $CLOUDIFY_URL -O ~/cloudify.zip || error_exit $? "Failed downloading cloudify installation"

echo Unzip cloudify installation
unzip ~/cloudify.zip > /dev/null
rm -rf ~/cloudify.zip

echo Starting Cloudify bootstrap-localcloud `hostname -I`
nohup ~/gigaspaces-cloudify-2.2.0-ga/bin/cloudify.sh "bootstrap-localcloud -nic-address `hostname -I`"

cat nohup.out
exit 0