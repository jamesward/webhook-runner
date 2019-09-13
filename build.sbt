enablePlugins(PlayScala)
disablePlugins(PlayFilters)

name := "webhook-runner"

scalaVersion := "2.13.0"

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % "test",
)

sources in (Compile,doc) := Seq.empty

testOptions in Test += Tests.Argument("-oF")
