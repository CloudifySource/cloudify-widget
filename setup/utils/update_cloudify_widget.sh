source load_sysconfig.sh

perform_git_pull(){
    git pull
    if [ "$?" -ne "0" ]; then       # we need to consider using hard reset here instead of specifying there's a problem: git reset --hard
        echo "problems with git pull, run git status to see the problem"
        exit 1
    fi

    echo "creating changelog"
    git log --oneline --abbrev=30 ORIG_HEAD.. >> ${WIDGET_HOME}/automatic_changelog
}

# assume there are no conflicts
echo "pulling source from git repository"
CURRENT_DIRECTORY=`pwd`

echo "going to  [$WIDGET_HOME] to perform git pull"
cd ${WIDGET_HOME}
perform_git_pull

echo "make log directories"
mkdir -p /var/log/cloudifyWidget/cli


echo "going to  [$MODULES_HOME] to perform git pull"
cd ${MODULES_HOME}
git remote update;
HAS_UPDATES=`git pull --dry-run | grep -q -v 'Already up-to-date.' | wc -l`
echo "has updates is $HAS_UPDATES"
if [ "$HAS_UPDATES" != "0" ]; then
    perform_git_pull
    echo "building modules"
    mvn clean install

    echo "updating custom softlayer jar"
    cd bin
    ./maven-install-custom.sh
    cd ..

    echo "deleting cloudify.widget artifacts from $PLAY_HOME/repository/cache/cloudify.widget/"
    rm -Rf $PLAY_HOME/repository/cache/cloudify.widget/
else
    echo "modules - already-up-to-date"
fi

echo "creating nohup link"
ln -Tfs /var/log/cloudifyWidget/nohup.out  ${WIDGET_HOME}/nohup.out
cd $CURRENT_DIRECTORY