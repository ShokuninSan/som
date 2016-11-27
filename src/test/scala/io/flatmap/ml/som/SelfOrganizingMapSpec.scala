package io.flatmap.ml.som

import breeze.linalg.DenseMatrix
import org.apache.spark.mllib.random.RandomRDDs
import org.scalatest._
import util.TestSparkContext

class SelfOrganizingMapSpec extends FlatSpec with Matchers with BeforeAndAfterEach with TestSparkContext {

  "instantiation" should "create a SOM with codebook of zeros" in {
    val som = new SelfOrganizingMap(6, 6)
    som.codebook should === (DenseMatrix.zeros[Array[Double]](6, 6))
  }

  "initialize" should "copy random data points from RDD into codebook" in {
    val data = RandomRDDs.normalVectorRDD(sparkSession.sparkContext, numRows = 512L, numCols = 3)
    val som = new SelfOrganizingMap(6, 6)
    som.initialize(data)
    som.codebook should !== (DenseMatrix.zeros[Array[Double]](6, 6))
  }

}
