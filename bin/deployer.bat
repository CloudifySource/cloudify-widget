set SERVER_IP=%1
set RECIPE_URL=%2
set RECIPE_TYPE=%3
set GS_HOME=gigaspaces-cloudify-2.2.0-ga

cd %GS_HOME%/bin

./cloudify.bat "connect http://%SERVER_IP%:8100;%RECIPE_TYPE% %RECIPE_URL%"

exit 1
