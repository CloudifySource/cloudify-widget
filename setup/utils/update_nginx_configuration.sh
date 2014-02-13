
if [ -f /etc/sysconfig/widget ];then
    source /etc/sysconfig/widget
fi

TMP_SITE_CONF=${WIDGET_HOME}/conf/nginx/output.nginx
SITE_CONF_TARGET=/etc/nginx/sites-available/$SITE_DOMAIN
NGINX_CONF_SRC=${WIDGET_HOME}/conf/nginx/nginx.conf
NGINX_CONF_TARGET=/etc/nginx/nginx.conf
echo "copying nginx configurations"

cmp  -s ${NGINX_CONF_SRC} ${NGINX_CONF_TARGET}
if [ $? -ne 0 ]; then
    \cp -f ${NGINX_CONF_SRC} ${NGINX_CONF_TARGET}
    service nginx restart
else
     echo "nginx configuration did not change, not copying"
fi

cat ${WIDGET_HOME}/conf/nginx/site.nginx  | sed 's/__domain_name__/'"$SITE_DOMAIN"'/' | sed 's/__staging_name__/'"$SITE_STAGING_DOMAIN"'/' > ${TMP_SITE_CONF}
cmp  -s ${TMP_SITE_CONF}  ${SITE_CONF_TARGET}
if [ $? -eq 1 ]; then
    \cp -f ${TMP_SITE_CONF} ${SITE_CONF_TARGET}
    echo "restarting nginx"
    service nginx restart
else
    echo "nginx configuration did not change, not restarting"
fi

\rm -f ${TMP_SITE_CONF}