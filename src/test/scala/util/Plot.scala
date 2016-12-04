package util

import java.awt.Color

import breeze.plot._
import io.flatmap.ml.som.SelfOrganizingMap.CodeBook

import scala.util.Try

object Plot {

  def rgb(title: String, cb: CodeBook, path: String) = Try {
    val f = Figure(title)
    val rgb: List[Double] => Double = {
      case red :: green :: blue :: Nil =>
        new Color((red*255.0).toInt, (green*255.0).toInt, (blue*255.0).toInt).getRGB.toDouble
      case _ => Color.white.getRGB.toDouble
    }
    f.subplot(0) += image(cb.mapValues(x => rgb(x.toList)))
    f.saveas(path)
  }

}
