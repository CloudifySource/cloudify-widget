source load_sysconfig.sh
echo "updating logrotate configuration"
\cp -f ${WIDGET_HOME}/conf/logrotate/logrotate.conf /etc/logrotate.d/cloudifyWidget