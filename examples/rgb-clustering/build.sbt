import AssemblyKeys._

name := """rgb-clustering"""

version := "1.0.0"

scalaVersion := "2.11.7"

val sparkVersion = "2.0.2"

val breezeVersion = "0.11.2"

libraryDependencies ++= Seq(
  "io.flatmap" %% "som" % "1.0.0-SNAPSHOT",
  "org.scalanlp" % "breeze_2.11" % breezeVersion,
  "org.scalanlp" % "breeze-viz_2.11" % breezeVersion,
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided"
)

assemblySettings
