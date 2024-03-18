scalaVersion := "3.4.0"

enablePlugins(PackPlugin)

Compile / run / fork := true

libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.9.3"
libraryDependencies += "com.outr" %% "scribe" % "3.13.0"
libraryDependencies += "com.outr" %% "scribe-cats" % "3.13.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.5.4"

libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.9.3" % Test
