import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "cloudify-widget"
    val appVersion      = "1.0-SNAPSHOT"

    val springVersion = "3.2.0.RELEASE"
    val springPackage = "org.springframework"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "commons-io" % "commons-io" % "2.4"  ,
      "commons-collections" % "commons-collections" % "3.0"   ,
      "org.apache.commons" % "commons-exec" % "1.1"  ,
      "commons-configuration" % "commons-configuration" % "1.9",

      springPackage % "spring-context" % springVersion,
      springPackage % "spring-core" % springVersion,
      springPackage % "spring-beans" % springVersion,


      "com.thoughtworks.xstream" % "xstream" % "1.4.3"  ,
      "org.reflections" % "reflections" % "0.9.8"  ,
      "com.google.code.gson" % "gson" % "2.2.2"  ,
      "commons-validator" % "commons-validator" % "1.4.0",
      "mysql" % "mysql-connector-java" % "5.1.18"  ,
      "commons-lang" % "commons-lang" % "2.3",
      "com.mixpanel" % "mixpanel-java" % "1.0.1",
      "org.openid4java" % "openid4java" % "0.9.7",
      "org.jasypt" % "jasypt" % "1.9.0",
      "org.apache.commons" % "commons-email" % "1.2",



      "cloudify.widget" % "api" % "1.0.0",
      "cloudify.widget" % "softlayer" % "1.0.0",
      "cloudify.widget" % "hpcloud" % "1.0.0",
      "cloudify.widget" % "all-clouds" % "1.0.0",
      "cloudify.widget" % "cli" % "1.0.0"

    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      // Add your own project settings here
      // resolvers += "TAMU Release Repository" at "https://maven.library.tamu.edu/content/repositories/releases/";
        resolvers +=  "Local Maven" at Path.userHome.asFile.toURI.toURL + ".m2/repository"

    )

}
