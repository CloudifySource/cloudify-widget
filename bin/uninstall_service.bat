
@set SERVER_IP=%1
@set INSTALL_NAME=%2
echo "ignore: CLOUDIFY_HOME is %CLOUDIFY_HOME%"
cd /d %CLOUDIFY_HOME%


call bin\cloudify.bat "connect http://%SERVER_IP%:8100; use-application default; uninstall-service %INSTALL_NAME%"


