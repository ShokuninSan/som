package util

import breeze.linalg.DenseMatrix
import io.flatmap.ml.som.NeighborhoodKernel
import io.flatmap.ml.som.SelfOrganizingMap.Parameters

trait FakeNeighborhoodKernel extends NeighborhoodKernel {

  override def neighborhood(winner: (Int, Int))(implicit p: Parameters): DenseMatrix[Double] = ???

}
