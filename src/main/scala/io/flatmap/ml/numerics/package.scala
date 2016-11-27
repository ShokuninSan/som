package io.flatmap.ml

import breeze.linalg.{*, DenseMatrix, DenseVector}

package object numerics {

  def outer[T](x: DenseVector[T], y: DenseVector[T]): DenseMatrix[T] = {
    val g = DenseMatrix.zeros[Double](y.length, x.length)
    g(::, *) := y
    g(*, ::) *:* x
  }

}
