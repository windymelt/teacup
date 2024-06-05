import smithy4s.codegen.Smithy4sCodegenPlugin

import scala.scalanative.build._

val scala3Version = "3.4.2"
val http4sVersion = "0.23.27"

usePipelining := true

nativeConfig ~= { c =>
  c.withLTO(LTO.none) // thin
    .withMode(Mode.debug) // releaseFast
    .withGC(GC.immix) // commix
}

lazy val common = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("common"))
  .enablePlugins(Smithy4sCodegenPlugin)
  .settings(
    scalaVersion := scala3Version,
    libraryDependencies += "com.disneystreaming.smithy4s" %%% "smithy4s-core" % smithy4sVersion.value,
    Compile / smithy4sInputDirs := Seq(
      baseDirectory.value / "../src/main/smithy", // Without this, generator finds smithy file in .jvm and .native
    ),
  )

lazy val daemon = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("daemon"))
  .dependsOn(common)
  .settings(
    scalaVersion := scala3Version,
    Compile / run / fork := true,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "os-lib" % "0.10.2",
      "com.outr" %% "scribe" % "3.13.0",
      "com.outr" %% "scribe-cats" % "3.13.0",
      "org.typelevel" %% "cats-effect" % "3.5.4",
      "com.lihaoyi" %% "os-lib" % "0.10.2" % Test,
    ),
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      // we cannot use Scala Native because support for SN 0.5.x is not yet.
      // os.spawn requires SN 0.5.x.
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "com.github.jnr" % "jnr-unixsocket" % "0.38.22",
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s" % smithy4sVersion.value,
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s-swagger" % smithy4sVersion.value,
    ),
  )

lazy val command = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .enablePlugins(PackPlugin)
  .in(file("command"))
  .dependsOn(common)
  .settings(
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.http4s" %%% "http4s-ember-client" % http4sVersion,
      "com.disneystreaming.smithy4s" %%% "smithy4s-http4s" % smithy4sVersion.value,
      "com.outr" %%% "scribe" % "3.13.0",
      "com.outr" %%% "scribe-cats" % "3.13.0",
      "org.typelevel" %%% "cats-effect" % "3.5.4",
    ),
  )
