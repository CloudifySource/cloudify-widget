
@set CLOUD_NAME=%1
@set GS_HOME= d:/GigaSpaces/gigaspaces-cloudify-2.3.0-ga/
 
cd /d %GS_HOME%tools/cli/
call cloudify.bat bootstrap-cloud --verbose %CLOUD_NAME%