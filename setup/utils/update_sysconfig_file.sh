# assuming sysconfig_play exists on machine
echo "copying sysconfig file"
\cp -f ${SYSCONF_FILE} /etc/sysconfig/widget
. /etc/sysconfig/widget