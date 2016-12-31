# SOM [![Build Status](https://travis-ci.org/ShokuninSan/som.svg?branch=master)](https://travis-ci.org/ShokuninSan/som) [![codecov.io](https://codecov.io/github/ShokuninSan/som/coverage.svg?branch=master)](https://codecov.io/github/ShokuninSan/som?branch=master)

A simple Self Organizing Map for Scala and Apache Spark.

## Usage
Make sure you have an implicit `SparkSession` and your data RDD ready.
```scala
implicit val sparkSession = ???

val data: RDD[Vector] = ???
```
Compose your own SOM instance, with either predefined or custom implementations of decay functions, neighborhood kernels or error metrics... 
```scala
val SOM = new SelfOrganizingMap with CustomDecay with GaussianNeighborboodKernel with QuantizationErrorMetrics {
    override val shape: Shape = (24, 24)
    override val learningRate: Double = 0.3
    override val sigma: Double = 0.5
  }
```
... or just use an off-the-shelf SOM for your convenience.
```scala
val SOM = GaussianSelfOrganizingMap(24, 24, sigma = 0.5, learningRate = 0.3)
```
Initialization and training:
```scala
val (som, params) = SOM.initialize(data).train(data, 20)
```
You can find more examples using the SOM library in the tests and complete applications in the `examples` directory.

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

Some parts of the implementation are inspired by the [spark-som](https://github.com/PragmaticLab/spark-som) project. Credits to @jxieeducation / PragmaticLab.
