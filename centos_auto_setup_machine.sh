
#! /bin/bash
echo "installing java"
yum  -y install java-1.6.0-openjdk-devel
export JAVA_HOME=/usr/lib/jvm/java-1.6.0-openjdk.x86_64

echo "downloading play"
if [ -e play-2.0.4.zip ]; then
    echo "play already exists - nothing to do"
else
    wget 'http://download.playframework.org/releases/play-2.0.4.zip'
    unzip play-2.0.4.zip
fi

echo "downloading cloudify"
CLOUDIFY_FOLDER=gigaspaces-cloudify-2.3.0-ga-b3510
CLOUDIFY_FILE=${CLOUDIFY_FOLDER}.zip
if [ -e $CLOUDIFY_FILE ]; then
    echo "cloudify already installed, nothing to go"
else
    wget 'http://repository.cloudifysource.org/org/cloudifysource/2.3.0-RELEASE/${CLOUDIFY_FILE}'
    unzip $CLOUDIFY_FILE
fi

echo "installing git"
yum  -y install git
cd play-2.0.4

echo "cloning widget"
git clone https://github.com/CloudifySource/cloudify-widget.git

echo "installing ruby"
yum install -y ruby rubygems

echo "installing sass"
gem install sass

# assuming sysconfig_play exists on machine
echo "copying sysconfig file"
\cp -f ~/sysconfig_play /etc/sysconfig/play
. /etc/sysconfig/play

# assuming there is a prod.conf copied to here
echo "copying configuration files"
\cp -f ~/prod.conf cloudify-widget/conf
\cp -f ~/hpcloud.pem cloudify-widget/bin
ln -fs ~/$CLOUDIFY_FILE bin/cloudify-folder # create a symbolic link to cloudify home.
chmod 755 cloudify-widget/*.sh
chmod 755 cloudify-widget/bin/*.sh
ln -s /root/$CLOUDIFY_FOLDER cloudify-widget/cloudify-folder

#install mysql
echo "installing mysql"
yum -y install mysql-server mysql php-mysql
chkconfig --levels 235 mysqld on
service mysqld start
mysql -u $DB_ADMIN -e "SET PASSWORD FOR '$DB_ADMIN'@'localhost' = PASSWORD('$DB_ADMIN_PASSWORD');"
mysql -u $DB_ADMIN -p$DB_ADMIN_PASSWORD -e "SET PASSWORD FOR '$DB_ADMIN'@'127.0.0.1' = PASSWORD('$DB_ADMIN_PASSWORD');"
mysql -u $DB_ADMIN -e "SET PASSWORD FOR '$DB_ADMIN'@'127.0.0.1' = PASSWORD('$DB_ADMIN_PASSWORD');"
mysql -u $DB_ADMIN -p$DB_ADMIN_PASSWORD -e "SET PASSWORD FOR '$DB_ADMIN'@'localhost.localdomain' = PASSWORD('$DB_ADMIN_PASSWORD');"
mysql -u $DB_ADMIN -e "SET PASSWORD FOR '$DB_ADMIN'@'localhost.localdomain' = PASSWORD('$DB_ADMIN_PASSWORD');"
mysql -u $DB_ADMIN -p$DB_ADMIN_PASSWORD -e "DROP USER ''@'localhost';"
mysql -u $DB_ADMIN -e "DROP USER ''@'localhost';"
mysql -u $DB_ADMIN -p$DB_ADMIN_PASSWORD  -e  "DROP USER ''@'localhost.localdomain';"
mysql -u $DB_ADMIN -e "DROP USER ''@'localhost.localdomain';"

cd cloudify-widget

echo "creating DB scheme"
bin/migrate_db.sh create
echo "127.0.0.1 `hostname`" >> /etc/hosts
ln -s ~/play-2.0.4/play /usr/bin/play


# install nginx
echo "installing nginx"
cp conf/nginx/install.conf /etc/yum.repos.d/nginx.repo
yum -y install nginx
mv /etc/nginx/nginx.conf /etc/nginx/nginx_conf_backup
cp conf/nginx/nginx.conf /etc/nginx/

# copy nginx configuration while sed-ing the domain names
mkdir -p /var/log/nginx/$SITE_DOMAIN
mkdir -p /etc/nginx/sites-available
mkdir -p /etc/nginx/sites-enabled
cat conf/nginx/site.nginx  | sed 's/__domain_name__/'"$SITE_DOMAIN"'/' | sed 's/__staging_name__/'"$SITE_STAGING_DOMAIN"'/' > /etc/nginx/sites-available/$SITE_DOMAIN
ln -s  /etc/nginx/sites-available/$SITE_DOMAIN /etc/nginx/sites-enabled/$SITE_DOMAIN
service nginx restart

echo "creating error pages"
# create path /var/www/cloudifyWidget/public/error_pages
mkdir -p /var/www/cloudifyWidget/public/error_pages

echo "intalling monit"
\cp -Rf conf/monit/repo  /etc/yum.repos.d/epel.repo
yum clean all
yum -y install monit
chkconfig --levels 235 monit on


echo "upgrading system"
./upgrade_server.sh

echo "installing takipi"
cd ~
wget https://s3.amazonaws.com/app-takipi-com/deploy/linux/takipi-install
chmod +x ./takipi-install
./takipi-install

# The "setup" process assumes there are no JVM installed with Takipi on them.
# However, if we are not setting up a new environment, but changing existing environment,
# we need to follow these steps :
# 1. install Takipi, then stop the daemon by running: /etc/takipi/takipi-stop
# 2. open this file using a text editor (with root privileges): /var/lib/takipi/work/service.key
# 3. paste old key
# 4. restart daemon using: /etc/takipi/takipi-start
# And proceed normally.


#we already have a key?
if [ -z $TAKIPI_KEY ]; then
    echo "reusing takipi key"
    /etc/takipi/takipi-stop
    echo $TAKIPI_KEY >> /var/lib/takipi/work/service.key
    /etc/takipi/takipi-start
else
    echo "we do not have existing takipi key, lets use the new one"
    TAKIPI_KEY=`cat /var/lib/takipi/work/service.key`
    echo -e "\nTAKIPI_KEY=${TAKIPI_KEY}" >> /etc/sysconfig/play
fi

echo "to see takipi information go to  https://app.takipi.com with ${TAKIPI_KEY}"







