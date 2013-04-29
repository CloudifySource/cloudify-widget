
@set SERVER_IP=%1
@set RECIPE_URL=%2
@set RECIPE_TYPE=%3
@set INSTALL_NAME=%4
 
cd /d %CLOUDIFY_HOME%

tools\cli\cloudify.bat "connect http://%SERVER_IP%:8100;%RECIPE_TYPE% -name %INSTALL_NAME%  %RECIPE_URL% "
