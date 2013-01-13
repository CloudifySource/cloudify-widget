
@set CLOUD_NAME=%1
 
cd /d d:/GigaSpaces/gigaspaces-cloudify-2.5.0-m5/tools/cli/
cloudify.bat bootstrap-cloud --verbose %CLOUD_NAME% 