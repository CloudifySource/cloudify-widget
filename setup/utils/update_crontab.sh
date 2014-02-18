CURRENT_DIRECTORY=`pwd`

cd "$(dirname "$0")"

source load_sysconfig.sh

if [ -z "$CRON_EMAIL" ]; then
    echo "no cron email set.. emails will not be sent from cron"
fi

cp ${WIDGET_HOME}/conf/cron/cron.conf /tmp/cronconf

crontab ${WIDGET_HOME}/conf/cron/cron.conf

cd $CURRENT_DIRECTORY