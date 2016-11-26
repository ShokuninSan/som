import AssemblyKeys._

name := """som"""

organization := "io.flatmap"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.7"

val sparkVersion = "2.0.2"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.scalanlp" % "breeze_2.11" % "0.13-RC1",
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided"
)

assemblySettings

parallelExecution in Test := false