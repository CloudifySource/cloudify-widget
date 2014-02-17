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

cd ${MODULES_HOME}
perform_git_pull
echo "building modules"
mvn install
echo "deleting cloudify.widget artifacts from $PLAY_HOME/repository/cache/cloudify.widget/"
rm -Rf $PLAY_HOME/repository/cache/cloudify.widget/


cd $CURRENT_DIRECTORY