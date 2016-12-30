package io.flatmap.ml.som

import breeze.linalg.DenseMatrix
import io.flatmap.ml.som.SelfOrganizingMap.CodeBook
import org.apache.spark.mllib.linalg.DenseVector
import org.apache.spark.mllib.random.RandomRDDs
import org.scalatest._
import util.{FakeDecayFunction, FakeMetrics, FakeNeighborhoodKernel, TestSparkContext}

class SelfOrganizingMapSpec extends FlatSpec with Matchers with BeforeAndAfterEach with TestSparkContext {

  def SOM(width: Int, height: Int) =
    new SelfOrganizingMap with FakeNeighborhoodKernel with FakeDecayFunction with FakeMetrics {
      override val learningRate: Double = 0.1
      override val sigma: Double = 0.2
      override var codeBook: CodeBook = DenseMatrix.fill[Array[Double]](height, width)(Array.emptyDoubleArray)
    }

  "instantiation" should "create a SOM with codebook of zeros" in {
    val som = SOM(6, 6)
    som.codeBook should === (DenseMatrix.fill[Array[Double]](6, 6)(Array.emptyDoubleArray))
  }

  "initialize" should "copy random data points from RDD into codebook" in {
    val data = RandomRDDs.normalVectorRDD(sparkSession.sparkContext, numRows = 512L, numCols = 3)
    val som = SOM(6, 6)
    som.initialize(data).codeBook should !== (DenseMatrix.fill[Array[Double]](6, 6)(Array.emptyDoubleArray))
  }

  "winner" should "return best matching unit (BMU)" in {
    val som = SOM(6, 6)
    som.codeBook.keysIterator.foreach { case (x, y) => som.codeBook(x, y) = Array(0.2, 0.2, 0.2) }
    som.codeBook(3, 3) = Array(0.3, 0.3, 0.3)
    som.winner(new DenseVector(Array(2.0, 2.0, 2.0)), som.codeBook) should equal ((3, 3))
    som.winner(new DenseVector(Array(0.26, 0.26, 0.26)), som.codeBook) should equal ((3, 3))
  }

  "winner" should "return last best matching unit (BMU) index in case of multiple BMUs" in {
    val som = SOM(6, 6)
    som.codeBook.keysIterator.foreach { case (x, y) => som.codeBook(x, y) = Array(0.2, 0.2, 0.2) }
    som.codeBook(3, 3) = Array(0.3, 0.3, 0.3)
    som.winner(new DenseVector(Array(0.25, 0.25, 0.25)), som.codeBook) should equal ((5, 5))
  }

}
