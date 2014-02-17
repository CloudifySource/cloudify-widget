echo "downloading play"

source load_sysconfig.sh

if [ -f /usr/bin/play ]; then
    echo "play already exists - nothing to do"
else
    wget 'http://download.playframework.org/releases/play-2.0.4.zip'  -O /tmp/play.zip
    unzip -o /tmp/play.zip -d /usr/lib
    ln -Tfs ${PLAY_HOME} /usr/lib/play
    rm -f /tmp/play.zip
fi
echo "setting a link to play"
ln -Tfs /usr/lib/play/play /usr/bin/play