package io.flatmap.ml

import breeze.linalg.{*, DenseMatrix, DenseVector}
import breeze.math.Field
import breeze.numerics._

import scala.reflect._

package object numerics {

  def outer[T:ClassTag:Field](x: DenseVector[T], y: DenseVector[T]): DenseMatrix[T] = {
    val g = DenseMatrix.zeros[T](y.length, x.length)
    g(::, *) := y
    g(*, ::) *:* x
  }

}
