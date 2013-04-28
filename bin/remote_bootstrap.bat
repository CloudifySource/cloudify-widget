
@set CLOUD_NAME=%1
echo "bootstrapping on %CLOUD_NAME%"
echo "running with %CLOUDIFY_HOME%"
cd /d %CLOUDIFY_HOME%
call tools\cli\cloudify.bat bootstrap-cloud --verbose %CLOUD_NAME%