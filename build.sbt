ThisBuild / version := "1.0.0"

ThisBuild / scalaVersion := "2.13.16"

lazy val root = (project in file("."))
  .settings(
    name := "bg3-banks-generator"
  ).enablePlugins(AssemblyPlugin)

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.4.0"
libraryDependencies += "commons-cli" % "commons-cli" % "1.10.0"

assembly / assemblyOutputPath := file("deploy/lib/bg3-banks-gen.jar")
assembly / mainClass := Some("BG3BanksGenerator")