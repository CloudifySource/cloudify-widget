import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "cloudify-widget"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "commons-io" % "commons-io" % "2.4"  ,
      "commons-collections" % "commons-collections" % "3.0"   ,
      "org.apache.commons" % "commons-exec" % "1.1"  ,
    "commons-configuration" % "commons-configuration" % "1.9",
      "org.apache.jclouds" % "jclouds-allcompute" % "1.6.2-incubating"  ,
      "org.apache.jclouds.driver" % "jclouds-sshj" % "1.6.2-incubating"  ,
      "com.thoughtworks.xstream" % "xstream" % "1.4.3"  ,
      "org.reflections" % "reflections" % "0.9.8"  ,
      "com.google.code.gson" % "gson" % "2.2.2"  ,
      "com.google.guava" % "guava" % "13.0.1"  ,
      "commons-validator" % "commons-validator" % "1.4.0",
      "play" % "spring_2.9.1" % "2.0"  ,
      "mysql" % "mysql-connector-java" % "5.1.18"  ,
      "commons-lang" % "commons-lang" % "2.3",
      "com.typesafe" %% "play-plugins-mailer" % "2.0.4",
      "com.mixpanel" % "mixpanel-java" % "1.0.1",
      "org.openid4java" % "openid4java" % "0.9.7",
      "org.jasypt" % "jasypt" % "1.9.0"

    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      // Add your own project settings here
       resolvers += "TAMU Release Repository" at "https://maven.library.tamu.edu/content/repositories/releases/"
    )

}
