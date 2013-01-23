
@set SERVER_IP=%1
@set RECIPE_URL=%2
@set RECIPE_TYPE=%3
 
cd /d %CLOUDIFY_HOME%tools/cli/
call cloudify.bat "connect http://%SERVER_IP%:8100;%RECIPE_TYPE% %RECIPE_URL%"
