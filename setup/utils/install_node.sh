if [ -f /usr/bin/node ]; then
    echo "node already installed"
else
    CURRENT_DIR=`pwd`
    mkdir /usr/lib/node-nave
    cd /usr/lib/node-nave
    if [ ! -f nave.sh ]; then
        echo "downloading nave"
        wget https://raw.github.com/isaacs/nave/master/nave.sh
    fi
    chmod 755 nave.sh
    ./nave.sh install 0.10.18
    ln -Tfs ~/.nave/installed/0.10.18/bin/node /usr/bin/node
    ln -Tfs ~/.nave/installed/0.10.18/bin/npm /usr/bin/npm
    cd $CURRENT_DIR
fi