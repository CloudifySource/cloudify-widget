echo "I will update sysconf"
source load_sysconfig.sh

if [ -z $SYSCONF_URL ] || [ "$SYSCONF_URL" = "" ]; then
    echo "ME_CONF_URL not set. I will not update me conf"
else
    echo "downloading me.conf from [$SYSCONF_URL]"
    wget "$SYSCONF_URL" -O /etc/sysconfig/cloudifyWidget
fi