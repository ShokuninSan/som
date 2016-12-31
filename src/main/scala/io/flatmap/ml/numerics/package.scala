package io.flatmap.ml

import breeze.linalg.{*, DenseMatrix, DenseVector}
import breeze.math.Field

import scala.reflect._

package object numerics {

  /** Computes the outer product of two vectors
    *
    * @tparam T type of elements of the vectors
    * @param x first [[breeze.linalg.DenseVector]]
    * @param y second [[breeze.linalg.DenseVector]]
    * @return a [[breeze.linalg.DenseMatrix]] as outer product
    */
  def outer[T:ClassTag:Field](x: DenseVector[T], y: DenseVector[T]): DenseMatrix[T] = {
    val g = DenseMatrix.zeros[T](y.length, x.length)
    g(::, *) := y
    g(*, ::) :* x
  }

}
