package example

import java.awt.Color

import io.flatmap.ml.som.SelfOrganizingMap
import io.flatmap.ml.util.Plot
import org.apache.spark.mllib.linalg.DenseVector
import org.apache.spark.sql.SparkSession

object RGB {

  def main(args: Array[String]): Unit = {
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
    Plot.errors(params.errors.reverse)
    Plot.som(f"Trained SOM (error=${params.errors.head}%1.4f)", som.codeBook, "trained_som.png") {
      case red :: green :: blue :: Nil =>
        new Color((red*255.0).toInt, (green*255.0).toInt, (blue*255.0).toInt).getRGB.toDouble
      case _ => Color.white.getRGB.toDouble
    }
  }

}
