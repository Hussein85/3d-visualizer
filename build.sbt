name := "m"

version := "1.0.2-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.10.4"

EclipseKeys.withSource := true

resolvers ++= Seq(
  "Sonatype Nexus Repository Manager Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Spray IO Release Repo" at "http://repo.spray.io",
  "Rhinofly Internal Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-release-local",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

resolvers += Resolver.file("Local repo", file(System.getProperty("user.home") + "/.ivy2/local"))(Resolver.ivyStylePatterns)

libraryDependencies ++= Seq(
  jdbc,
  cache,
  "org.webjars" %% "webjars-play" % "2.3.0-3",
  "org.webjars" % "bootstrap" % "3.1.1",
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "com.typesafe.play" %% "play-slick" % "0.8.1",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "joda-time" % "joda-time" % "2.4",
  "org.joda" % "joda-convert" % "1.6",
  "com.github.tototoshi" %% "slick-joda-mapper" % "1.2.0",
  "ws.securesocial" %% "securesocial" % "3.0-M3",
  "ws.securesocial" %% "securesocial" % "3.0-M3" classifier "assets",
  "com.wordnik" %% "swagger-play2" % "1.3.12" exclude("org.reflections", "reflections"),
  "org.reflections" % "reflections" % "0.9.8" notTransitive (),
  "local" %% "play-s3" % "local-SNAPSHOT" 
)     
