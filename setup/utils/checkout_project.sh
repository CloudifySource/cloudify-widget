_CHECKOUT_GIT_LOCATION=$1
_CHECKOUT_GIT_BRANCH=$2
_CHECKOUT_HOME=$3


if [ -z $_CHECKOUT_GIT_LOCATION ] || [ $_CHECKOUT_GIT_LOCATION"xxx" = "xxx" ]; then
    echo "please define a github repository to checkout from"
    exit 1;
fi
echo "using git location : ${_CHECKOUT_GIT_LOCATION}"

if [ ! -f $_CHECKOUT_HOME ]; then

    if [ -f $_CHECKOUT_HOME/.widgetGitLocation ]; then
        LAST_GIT_LOCATION=`cat $_CHECKOUT_HOME/.widgetGitLocation`

        if [ "$LAST_GIT_LOCATION" != "$_CHECKOUT_GIT_LOCATION" ]; then
            echo "Last git location does not match"
            exit 1
        fi
    fi

    echo "cloning git repository from $_CHECKOUT_GIT_LOCATION"
    git clone $_CHECKOUT_GIT_LOCATION $_CHECKOUT_HOME

    if [ -z $_CHECKOUT_GIT_BRANCH  ] || [ $_CHECKOUT_GIT_BRANCH"xxx" = "xxx" ]; then
            echo "no branch specified"
    else
        echo "checking out branch ${GIT_BRANCH}"
        cd $_CHECKOUT_HOME
        git checkout $_CHECKOUT_GIT_BRANCH
        cd -
    fi

    echo $_CHECKOUT_GIT_LOCATION > ${WIDGET_HOME}/.widgetGitLocation

else
    echo "already checked out"
fi