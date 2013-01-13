
yum  -y install java-1.6.0-openjdk-devel
export JAVA_HOME=/usr/lib/jvm/java-1.6.0-openjdk.x86_64

wget 'http://download.playframework.org/releases/play-2.0.4.zip'
unzip play-2.0.4.zip

wget 'http://repository.cloudifysource.org/org/cloudifysource/2.2.0-RELEASE/gigaspaces-cloudify-2.2.0-ga-b2500.zip'
unzip gigaspaces-cloudify-2.2.0-ga-b2500.zip



yum  -y install git
cd play-2.0.4
git clone https://github.com/CloudifySource/cloudify-widget.git

yum install -y ruby rubygems
gem install sass

# assuming sysconfig_play exists on machine
cp ~/sysconfig_play /etc/sysconfig/play
. /etc/sysconfig/play

# assuming there is a prod.conf copied to here
mv ~/prod.conf cloudify-widget/conf
mv ~/hpcloud.pem cloudify-widget/bin
chmod 755 cloudify-widget/*.sh
chmod 755 cloudify-widget/bin/*.sh

#install mysql
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

bin/migrate_db.sh create
#find which is the latest version of DB
# ll all the files, remove "create" script, remove extension, sort in descending order and output first line.
db_version=`ls conf/evolutions/default -1 | grep -v create |  sed -e 's/\.[a-zA-Z]*$//' | sort -r | head -1`
bin/migrate_db.sh $db_version

echo "127.0.0.1 `hostname`" >> /etc/hosts
ln -s ~/play-2.0.4/play /usr/bin/play


# install nginx
cp cloudify-widget/conf/nginx/install.conf /etc/yum.repos.d/nginx.repo
yum -y install nginx

# copy nginx configuration while sed-ing the domain names
sed 's/__domain_name__/$SITE_DOMAIN/ __staging_name__/$SITE_STAGING_DOMAIN' cloudify-widget/conf/nginx/site.conf > /etc/nginx/sites-availabe/$SITE_DOMAIN

# create path /var/www/cloudifyWidget/public/error_pages
mkdir -p /var/www/cloudifyWidget/public/error_pages

# copy content from public error_pages to that path
cp -R cloudify-widget/public/errors_pages /var/www/cloudifyWidget/public/error_pages





