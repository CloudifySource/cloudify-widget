CURRENT_DIRECTORY=`pwd`

cd "$(dirname "$0")"

echo "intalling monit"

source load_sysconfig.sh

\cp -Rf ${WIDGET_HOME}/conf/monit/repo  /etc/yum.repos.d/epel.repo
yum clean all
yum -y install monit
chkconfig --levels 235 monit on

cd $CURRENT_DIRECTORY

