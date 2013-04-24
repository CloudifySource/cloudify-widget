
@set CLOUD_NAME=%1
 
cd /d %CLOUDIFY_HOME%tools/cli/
call cloudify.bat bootstrap-localcloud --verbose