if [ -z $GIT_LOCATION ] || [ $GIT_LOCATION"xxx" = "xxx" ]; then
    echo "please define a github repository to checkout from"
    exit 1;
fi
echo "using git location : ${GIT_LOCATION}"

if [ ! -f $WIDGET_HOME ]; then

    if [ -f $WIDGET_HOME/.widgetGitLocation ]; then
        LAST_GIT_LOCATION=`cat $WIDGET_HOME/.widgetGitLocation`

        if [ "$LAST_GIT_LOCATION" != "$GIT_LOCATION" ]; then
            echo "Last git location does not match"
            exit 1
        fi
    fi

    echo "cloning git repository from $GIT_LOCATION"
    git clone $GIT_LOCATION $WIDGET_HOME

    if [ -z $GIT_BRANCH  ] || [ $GIT_BRANCH"xxx" = "xxx" ]; then
            echo "no branch specified"
    else
        echo "checking out branch ${GIT_BRANCH}"
        cd $WIDGET_HOME
        git checkout $GIT_BRANCH
        cd -
    fi

    echo $GIT_LOCATION > ${WIDGET_HOME}/.widgetGitLocation

else
    echo "already checked out"
fi