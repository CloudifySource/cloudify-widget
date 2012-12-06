rem this file helps run play with personal configuration
rem the me.conf file should have as first line:
rem import "application.conf" (or some other file)
rem then you should start overriding application configuration
rem such as DB details and what not.
rem the "dev" folder is ignored by GIT, so fear not to commit it by mistake.
rem if you want to debug, simply run "play_run.bat debug".

play %* -Dconfig.file=conf/dev/me.conf run