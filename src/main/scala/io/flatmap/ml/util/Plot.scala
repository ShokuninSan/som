package io.flatmap.ml.util

import breeze.plot._
import io.flatmap.ml.som.SelfOrganizingMap._

import scala.util.Try

object Plot {

  def som(title: String, cb: CodeBook, path: String)(weightsMappingFn: List[Double] => Double) = Try {
    val f = Figure(title)
    f.subplot(0) += image(cb.mapValues(x => weightsMappingFn(x.toList)))
    f.saveas(path)
  }

}
