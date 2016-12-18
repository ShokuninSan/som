# SOM [![Build Status](https://travis-ci.org/ShokuninSan/som.svg?branch=master)](https://travis-ci.org/ShokuninSan/som) [![codecov.io](https://codecov.io/github/ShokuninSan/som/coverage.svg?branch=master)](https://codecov.io/github/ShokuninSan/som?branch=master)

A simple Self Organizing Map for Scala and Apache Spark.

## Build and publish the SOM library locally
```
➜  som git:(master) ✗ sbt
...
> publishLocal
...
> project macros
...
> publishLocal
...
[success] Total time: 10 s, completed Dec 18, 2016 4:02:19 PM
>
```

## Usage example
```scala
implicit val sparkSession =
  SparkSession
    .builder
    .appName("rgb-clustering")
    .getOrCreate()

val rgb = sparkSession.sparkContext
  .textFile("data/rgb.csv")
  .map(_.split(",").map(_.toDouble / 255.0))
  .map(new DenseVector(_))

val (som, params) =
  SelfOrganizingMap(24, 24, sigma = 0.5, learningRate = 0.3)
    .initialize(rgb)
    .train(rgb, 20)
```
You can find complete applications using the SOM library in the `examples` directory.

Some parts of the implementation are inspired by the [spark-som](https://github.com/PragmaticLab/spark-som) project. Credits to @jxieeducation / PragmaticLab.
