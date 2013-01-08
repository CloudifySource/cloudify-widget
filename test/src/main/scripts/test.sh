#!/bin/sh
# install maven
id=$1

cd /root
wget http://apache.spd.co.il/maven/maven-3/3.0.4/binaries/apache-maven-3.0.4-bin.tar.gz
tar xvf apache-maven-3.0.4-bin.tar.gz 
export M2_HOME=/root/apache-maven-3.0.4 
export M2=$M2_HOME/bin 
export PATH=$M2:$PATH

# install firefox
yum intall -y firefox

#install X server
yum install -y xorg-x11-server-Xvfb
Xvfb :1 -screen 0 1024x768x24 &
export DISPLAY=:1

#install hp managment cli - not working
curl -sL https://docs.hpcloud.com/file/hpcloud-1.4.0.gem > hpcloud-1.4.0.gem
gem install hpcloud-1.4.0.gem


#start the widget site
cd /root/play-2.0.4/cloudify-widget
./play_run.sh &

url=http://localhost:9000
while ! wget --spider $url 2>&1 | grep --quiet "200 OK"; do sleep 1; echo waiting for site...; done;
	
#start the tests
cd /root/play-2.0.4/cloudify-widget/test
mvn test -U -X


#rebuild the server
hpcloud servers:rebuild $id