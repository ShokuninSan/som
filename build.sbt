name := "som"

val sparkVersion = "2.0.2"
val breezeVersion = "0.11.2"
val scalatestVersion = "2.2.4"
val slf4jVersion = "1.7.21"
val scalametaVersion = "1.1.0"
val scalametaParadiseVersion = "3.0.0-M5"

lazy val commonSettings = Seq(
  organization := "io.flatmap.ml",
  scalaVersion := "2.11.8",
  scalacOptions += "-Xplugin-require:macroparadise",
  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  addCompilerPlugin("org.scalameta" % "paradise" % scalametaParadiseVersion cross CrossVersion.full)
)

lazy val somSettings = Seq(
  version := "0.1.0-SNAPSHOT",
  libraryDependencies ++= Seq(
    "org.scalanlp" % "breeze-viz_2.11" % breezeVersion,
    "org.slf4j" % "slf4j-api" % slf4jVersion,
    "org.scalatest" %% "scalatest" % scalatestVersion % "test",
    "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
    "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided"
  ),
  // include the macro classes and resources in the main jar
  mappings in (Compile, packageBin) ++= mappings.in(macros, Compile, packageBin).value,
  // include the macro sources in the main source jar
  mappings in (Compile, packageSrc) ++= mappings.in(macros, Compile, packageSrc).value
)

lazy val macros = (project in file("macros"))
  .settings(commonSettings:_*)
  .settings(libraryDependencies ++= Seq(
    "org.scalameta" %% "scalameta" % scalametaVersion,
    "org.scala-lang" % "scala-reflect" % scalaVersion.value
  ))

lazy val som = (project in file("."))
  .settings(commonSettings:_*)
  .settings(somSettings:_*)
  .dependsOn(macros)

parallelExecution in Test := false
