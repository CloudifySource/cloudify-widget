echo "i will update me.conf"

CURRENT_DIRECTORY=`pwd`

cd "$(dirname "$0")"


source load_sysconfig.sh

echo "ME_CONF_URL is [$ME_CONF_URL]"

if [ -z $ME_CONF_URL ] || [ "$ME_CONF_URL" = "" ]; then
    echo "ME_CONF_URL not set. I will not update me conf"
else
    echo "downloading me.conf from [$ME_CONF_URL]"
    mkdir -p ${WIDGET_HOME}/conf/dev
    wget "$ME_CONF_URL" -O ${WIDGET_HOME}/conf/dev/me.conf
fi


cd $CURRENT_DIRECTORY