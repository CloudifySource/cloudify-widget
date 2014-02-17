echo "upgrading monit configurations"
cat ${WIDGET_HOME}/conf/monit/conf.monit | sed 's,__monit_from__,'"$MONIT_FROM"',' | sed 's,__monit_to__,'"$MONIT_SET_ALERT"',' > /etc/monit.conf
\cp -f ${WIDGET_HOME}/conf/monit/mysql.monit /etc/monit.d/mysqld
MONIT_PIDFILE=$WIDGET_HOME/RUNNING_PID
cat ${WIDGET_HOME}/conf/monit/widget.monit | sed 's,__monit_pidfile__,'"$MONIT_PIDFILE"',' > /etc/monit.d/widget
