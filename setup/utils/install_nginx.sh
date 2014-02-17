# install nginx
echo "installing nginx"
cp ${WIDGET_HOME}/conf/nginx/install.conf /etc/yum.repos.d/nginx.repo
yum -y install nginx
mv /etc/nginx/nginx.conf /etc/nginx/nginx_conf_backup

# copy nginx configuration while sed-ing the domain names
mkdir -p /var/log/nginx/$SITE_DOMAIN
mkdir -p /etc/nginx/sites-available
mkdir -p /etc/nginx/sites-enabled
touch /etc/nginx/sites-available/$SITE_DOMAIN
ln -s  /etc/nginx/sites-available/$SITE_DOMAIN /etc/nginx/sites-enabled/$SITE_DOMAIN

service nginx restart