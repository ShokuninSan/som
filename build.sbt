name := """som"""

organization := "io.flatmap"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.7"

val sparkVersion = "2.0.2"

val breezeVersion = "0.11.2"

val scalatestVersion = "2.2.4"

libraryDependencies ++= Seq(
  "org.scalanlp" % "breeze-viz_2.11" % breezeVersion,
  "org.scalatest" %% "scalatest" % scalatestVersion % "test",
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided"
)

parallelExecution in Test := false