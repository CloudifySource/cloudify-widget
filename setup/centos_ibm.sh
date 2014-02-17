
execute(){
    echo "executing $1"
    ${WIDGET_HOME}/setup/utils/$1
}

echo "clone cloudify-modules"

execute install_java.sh

execute install_mysql.sh

execute install_nginx.sh

execute install_ruby.sh

execute install_cloudify.sh

execute install_play.sh

execute install_maven.sh

execute install_node.sh

execute install_monit.sh

execute create_schema.sh

execute upgrade.sh