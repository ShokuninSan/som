package util

import java.awt.Color

import breeze.plot._
import io.flatmap.ml.som.SelfOrganizingMap.CodeBook

object Plot {

  def rgb(title: String, cb: CodeBook, path: String): Unit = {
    val f = Figure(title)
    val rgb: List[Double] => Double = {
      case red :: green :: blue :: Nil =>
        new Color((red*255.0).toInt, (green*255.0).toInt, (blue*255.0).toInt).getRGB.toDouble
    }
    f.subplot(0) += image(cb.mapValues(x => rgb(x.toList)))
    f.saveas(path)
  }

}
