echo "downloading play"

CURRENT_DIRECTORY=`pwd`

cd "$(dirname "$0")"


source load_sysconfig.sh

if [ -z "$PLAY_HOME" ]; then
    echo "need to set PLAY_HOME"
    exit 1
fi


if [ -f /usr/bin/play ]; then
    echo "play already exists - nothing to do"
else
    if [ ! -f /tmp/play.zip ]; then
        wget 'http://download.playframework.org/releases/play-2.0.4.zip'  -O /tmp/play.zip
    fi
    unzip -q -o /tmp/play.zip -d /usr/lib
    ln -Tfs ${PLAY_HOME} /usr/lib/play
    rm -f /tmp/play.zip
fi
echo "setting a link to play"
ln -Tfs /usr/lib/play/play /usr/bin/play


cd $CURRENT_DIRECTORY