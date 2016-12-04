import AssemblyKeys._

name := """som"""

organization := "io.flatmap"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.7"

val sparkVersion = "2.0.2"

val breezeVersion = "0.13-RC1"

val scalatestVersion = "2.2.4"

libraryDependencies ++= Seq(
  "org.scalanlp" % "breeze_2.11" % breezeVersion,
  "org.scalanlp" % "breeze-viz_2.11" % breezeVersion % "test",
  "org.scalatest" %% "scalatest" % scalatestVersion % "test",
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided"
)

assemblySettings

parallelExecution in Test := false