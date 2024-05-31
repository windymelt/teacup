val scala3Version = "3.4.2"

enablePlugins(PackPlugin)

Compile / run / fork := true
usePipelining := true

lazy val daemon = project
  .in(file("daemon"))
  .settings(
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "os-lib" % "0.9.3",
      "com.outr" %% "scribe" % "3.13.0",
      "com.outr" %% "scribe-cats" % "3.13.0",
      "org.typelevel" %% "cats-effect" % "3.5.4",
      "com.lihaoyi" %% "os-lib" % "0.9.3" % Test,
    )
  )
