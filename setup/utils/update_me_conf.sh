echo "i will update me.conf"
source load_sysconfig.sh

if [ -z $ME_CONF_URL ] || [ "$ME_CONF_URL" = "" ]; then
    echo "ME_CONF_URL not set. I will not update me conf"
else
    echo "downloading me.conf from [$ME_CONF_URL]"
    mkdir -p ${WIDGET_HOME}/conf/dev
    wget "$ME_CONF_URL" -O ${WIDGET_HOME}/conf/dev/me.conf
fi