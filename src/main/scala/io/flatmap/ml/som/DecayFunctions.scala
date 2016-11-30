package io.flatmap.ml.som

trait DecayFunction {

  def decay(coefficient: Double, iteration: Int, maxIterations: Int): Double

}

trait CustomDecay extends DecayFunction {

  self: SelfOrganizingMap =>

  override def decay(coefficient: Double, iteration: Int, maxIterations: Int): Double = {
    coefficient/(1.0+iteration.toDouble)/maxIterations.toDouble
  }

}
