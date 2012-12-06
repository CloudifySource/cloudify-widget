import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "cloudify-widget"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "commons-io" % "commons-io" % "2.4",
      "org.apache.commons" % "commons-exec" % "1.1",
      "org.jclouds" % "jclouds-allcompute" % "1.5.1",
      "org.jclouds.driver" % "jclouds-sshj" % "1.5.0",
      "com.thoughtworks.xstream" % "xstream" % "1.4.3",
      "org.reflections" % "reflections" % "0.9.8",
      "com.google.code.gson" % "gson" % "2.2.2",
      "com.google.guava" % "guava" % "13.0.1",
      "play" % "spring_2.9.1" % "2.0",
      "mysql" % "mysql-connector-java" % "5.1.18",
      "commons-lang" % "commons-lang" % "2.3"

    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      // Add your own project settings here
       resolvers += "TAMU Release Repository" at "https://maven.library.tamu.edu/content/repositories/releases/"
    )

}
