echo "installing java"
yum  -y install java-1.6.0-openjdk-devel
export JAVA_HOME=/usr/lib/jvm/java-1.6.0-openjdk.x86_64
echo "export JAVA_HOME"
ln -Tfs $JAVA_HOME/bin/java /usr/bin/java
 ln -Tfs $JAVA_HOME/bin/javac /usr/bin/javac