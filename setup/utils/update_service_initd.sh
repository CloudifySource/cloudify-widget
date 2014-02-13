
echo "upgrading init.d script"
\cp -f ${WIDGET_HOME}/conf/initd/widget /etc/init.d/widget
chmod 755 /etc/init.d/widget