echo "cloning cloudify-widget"
CHECKOUT_FOLDER=cloudify-widget
if [ -z $GIT_LOCATION ] || [ $GIT_LOCATION"xxx" = "xxx" ]; then
    GIT_LOCATION="https://github.com/CloudifySource/cloudify-widget.git"
fi
echo "using git location : ${GIT_LOCATION}"

git clone $GIT_LOCATION $CHECKOUT_FOLDER

if [ -z $GIT_BRANCH  ] || [ $GIT_BRANCH"xxx" = "xxx" ]; then
        echo "no branch specified"
else
    echo "checking out branch ${GIT_BRANCH}"
    cd $CHECKOUT_FOLDER
    git checkout $GIT_BRANCH
    cd -
fi