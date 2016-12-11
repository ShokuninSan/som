import AssemblyKeys._

name := """rgb-clustering"""

version := "0.1.0"

scalaVersion := "2.11.7"

val sparkVersion = "2.0.2"

libraryDependencies ++= Seq(
  "io.flatmap.ml" %% "som" % "0.1.0-SNAPSHOT",
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided"
)

assemblySettings
