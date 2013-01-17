
@set SERVER_IP=%1
@set RECIPE_URL=%2
@set RECIPE_TYPE=%3

@set GS_HOME= d:/GigaSpaces/gigaspaces-cloudify-2.3.0-ga/
 
cd /d %GS_HOME%tools/cli/
call cloudify.bat "connect http://%SERVER_IP%:8100;%RECIPE_TYPE% %RECIPE_URL%"
