echo "intalling monit"
\cp -Rf ${WIDGET_HOME}/conf/monit/repo  /etc/yum.repos.d/epel.repo
yum clean all
yum -y install monit
chkconfig --levels 235 monit on

