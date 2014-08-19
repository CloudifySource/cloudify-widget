# assume with are in "cloudify-widget" folder
echo "copying error pages"
# copy content from public error_pages to that path
mkdir -p  /var/www/cloudifyWidget/public
\cp -Rvf ${WIDGET_HOME}/public-folder/error_pages /var/www/cloudifyWidget/public