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
yum install -y firefox

#install X server
yum install -y xorg-x11-server-Xvfb
Xvfb :1 -screen 0 1024x768x24 &
export DISPLAY=:1

#install hp managment cli - not working
curl -sL https://docs.hpcloud.com/file/hpcloud-1.4.0.gem > hpcloud-1.4.0.gem
gem install hpcloud-1.4.0.gem


#start the widget site
cd /root/play-2.0.4/cloudify-widget
sed -i "s/466999/$id/g" conf/cloudify.conf
play $* -Dconfig.file=conf/prod.conf run &