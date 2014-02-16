# copy nginx configuration while sed-ing the domain names
mkdir -p /var/log/nginx/$SITE_DOMAIN
mkdir -p /etc/nginx/sites-available
mkdir -p /etc/nginx/sites-enabled
touch /etc/nginx/sites-available/$SITE_DOMAIN
ln -s  /etc/nginx/sites-available/$SITE_DOMAIN /etc/nginx/sites-enabled/$SITE_DOMAIN

service nginx restart