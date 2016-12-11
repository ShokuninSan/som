name := """som"""

organization := "io.flatmap.ml"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.7"

val sparkVersion = "2.0.2"

val breezeVersion = "0.11.2"

val scalatestVersion = "2.2.4"

val slf4jVersion = "1.7.21"

libraryDependencies ++= Seq(
  "org.scalanlp" % "breeze-viz_2.11" % breezeVersion,
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.scalatest" %% "scalatest" % scalatestVersion % "test",
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided"
)

parallelExecution in Test := false