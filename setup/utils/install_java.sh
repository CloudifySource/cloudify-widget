echo "installing java"

CURRENT_DIRECTORY=`pwd`

cd "$(dirname "$0")"

INSTALL_JAVA_DIR=/usr/lib/jvm
mkdir -p $INSTALL_JAVA_DIR
cd $INSTALL_JAVA_DIR
wget -O jdk.bin --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/6u33-b03/jdk-6u33-linux-x64.bin
chmod 755 jdk.bin
echo "yes" | ./jdk.bin &>/dev/null
export JAVA_HOME=/usr/lib/jvm/jdk1.6.0_33
echo "JAVA_HOME is $JAVA_HOME"
rm -f jdk.bin
ln -Tfs $JAVA_HOME/bin/java /usr/bin/java
ln -Tfs $JAVA_HOME/bin/javac /usr/bin/javac

# commenting out old code.. it seems that guava has bugs on certain java versions. we need to control the version.

#yum  -y install java-1.6.0-openjdk-devel
#export JAVA_HOME=/usr/lib/jvm/java-1.6.0-openjdk.x86_64
#echo "export JAVA_HOME"
#ln -Tfs $JAVA_HOME/bin/java /usr/bin/java
# ln -Tfs $JAVA_HOME/bin/javac /usr/bin/javac




cd $CURRENT_DIRECTORY