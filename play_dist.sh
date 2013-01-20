# adding dist script even though the dist product does not work yet.
# see problem at : http://stackoverflow.com/q/14378450/1068746
# to make this work on windows: http://stackoverflow.com/questions/10455537/how-to-get-play-framework-running-in-msys-bash
play -Dconfig.file=conf/dev/prod.conf dist
unzip dist/*.zip -d dist
DIST_FOLDER=ls | grep -v "zip"
 \cp -Rf bin $DIST_FOLDER