package io.flatmap.ml.som

import java.awt.Color

import breeze.linalg.DenseMatrix
import breeze.numerics.closeTo
import io.flatmap.ml.util.Plot
import org.apache.spark.mllib.linalg.DenseVector
import org.apache.spark.mllib.random.RandomRDDs
import org.scalatest._
import util.TestSparkContext

class GaussianSelfOrganizingMapSpec extends FlatSpec with Matchers with BeforeAndAfterEach with TestSparkContext {

  "train" should "return a fitted SOM instance" in {
    val path = getClass.getResource("/rgb.csv").getPath
    val rgb = sparkSession.sparkContext
      .textFile(path)
      .map(_.split(",").map(_.toDouble / 255.0))
      .map(new DenseVector(_))
    val som = GaussianSelfOrganizingMap(6, 6, _sigma = 0.5, _learningRate = 0.3).initialize(rgb)
    val initialCodeBook = som.codeBook.copy
    val codeBookVectorToRGB: List[Double] => Double = {
      case red :: green :: blue :: Nil =>
        new Color((red*255.0).toInt, (green*255.0).toInt, (blue*255.0).toInt).getRGB.toDouble
      case _ => Color.white.getRGB.toDouble
    }
    Plot.som("Initial SOM", som.codeBook, "initial_som.png")(codeBookVectorToRGB)
    val (newSom, params) = som.train(rgb, 20)
    Plot.som(f"Trained SOM (error=${params.errors.head}%1.4f)", newSom.codeBook, "trained_som.png")(codeBookVectorToRGB)
    Plot.errors(params.errors.reverse)
    newSom.codeBook should not equal initialCodeBook
    assert(closeTo(params.errors.head, 0.15, relDiff = 1e-2))
  }

}
