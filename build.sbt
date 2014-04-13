name := "m"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  "org.webjars" %% "webjars-play" % "2.2.1-2",
  "org.webjars" % "bootstrap" % "3.1.1",
  "com.typesafe.slick" %% "slick" % "2.0.1",
  "org.slf4j" % "slf4j-nop" % "1.6.4"
)     

play.Project.playScalaSettings
