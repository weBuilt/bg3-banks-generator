ThisBuild / version := "1.0.0"

ThisBuild / scalaVersion := "2.13.16"

lazy val root = (project in file("."))
  .settings(
    name := "bg3-banks-generator"
  ).enablePlugins(AssemblyPlugin)

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.4.0"
libraryDependencies += "commons-cli" % "commons-cli" % "1.10.0"

assembly / assemblyOutputPath := file("deploy/lib/bg3-banks-gen.jar")
assembly / mainClass := Some("app.BG3BanksGenerator")

libraryDependencies += "org.scalafx" %% "scalafx" % "17.0.1-R26"

libraryDependencies ++= {
  // Determine OS version of JavaFX binaries
  lazy val osName = System.getProperty("os.name") match {
    case n if n.startsWith("Linux") => "linux"
    case n if n.startsWith("Mac") => "mac"
    case n if n.startsWith("Windows") => "win"
    case _ => throw new Exception("Unknown platform!")
  }
  Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
    .map(m => "org.openjfx" % s"javafx-$m" % "17" classifier osName)
}

libraryDependencies += "org.typelevel" %% "cats-core" % "2.13.0"

val circeVersion = "0.14.14"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)
libraryDependencies += "io.circe" % "circe-generic-extras_2.13" % "0.14.5-RC1"