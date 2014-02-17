echo "I will update sysconf"

CURRENT_DIRECTORY=`pwd`

cd "$(dirname "$0")"

source load_sysconfig.sh

if [ -z $SYSCONF_URL ] || [ "$SYSCONF_URL" = "" ]; then
    echo "SYSCONF_URL not set. I will not update sysconf"
else
    echo "downloading me.conf from [$SYSCONF_URL]"
    wget "$SYSCONF_URL" -O /etc/sysconfig/cloudifyWidget
fi

cd $CURRENT_DIRECTORY