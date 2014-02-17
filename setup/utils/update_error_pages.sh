# assume with are in "cloudify-widget" folder
echo "copying error pages"
# copy content from public error_pages to that path
\cp -Rvf ${WIDGET_HOME}/public/error_pages /var/www/cloudifyWidget/public