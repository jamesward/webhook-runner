enablePlugins(PlayScala)

name := "webhook-runner"

scalaVersion := "2.13.0"

libraryDependencies ++= Seq(
  guice,
  "com.google.cloud" % "google-cloud-compute" % "0.108.0-alpha",

  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % "test",
)

sources in (Compile,doc) := Seq.empty

dockerBaseImage := "adoptopenjdk/openjdk8"

testOptions in Test += Tests.Argument("-oF")
