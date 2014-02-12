# install nginx
echo "installing nginx"
cp conf/nginx/install.conf /etc/yum.repos.d/nginx.repo
yum -y install nginx
mv /etc/nginx/nginx.conf /etc/nginx/nginx_conf_backup