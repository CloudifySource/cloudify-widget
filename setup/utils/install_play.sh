echo "downloading play"
if [ -f /usr/bin/play ]; then
    echo "play already exists - nothing to do"
else
    wget 'http://download.playframework.org/releases/play-2.0.4.zip'
    unzip play-2.0.4.zip
fi
echo "setting a link to play"
ln -s ~/play-2.0.4/play /usr/bin/play