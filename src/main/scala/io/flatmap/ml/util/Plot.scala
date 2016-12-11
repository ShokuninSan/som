package io.flatmap.ml.util

import breeze.plot._
import breeze.linalg._
import io.flatmap.ml.som.SelfOrganizingMap._

import scala.util.Try

object Plot {

  def som(title: String, cb: CodeBook, path: String)(weightsMappingFn: List[Double] => Double) = Try {
    val f = Figure(title)
    f.subplot(0) += image(cb.mapValues(x => weightsMappingFn(x.toList)))
    f.saveas(path)
  }

  def errors(es: List[Double]) = Try {
    val f = Figure("Errors")
    val p = f.subplot(0)
    val x = linspace(0.0, es.length.toDouble, es.length)
    val errs = DenseVector(es.toArray)
    p += plot(x, errs, colorcode = "red", labels = (i) => f"${errs(i)}%1.4f")
    p.xlabel = "Epoch"
    p.ylabel = "Error"
    f.saveas("errors.png")
  }

}
