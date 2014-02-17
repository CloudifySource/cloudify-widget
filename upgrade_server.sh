CURRENT_DIR=`pwd`

cd "$(dirname "$0")"
echo "workdir is [`pwd`] , executed from $CURRENT_DIR"

source setup/utils/upgrade.sh

cd $CURRENT_DIR