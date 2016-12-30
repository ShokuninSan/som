package util

import io.flatmap.ml.som.DecayFunction

trait FakeDecayFunction extends DecayFunction {

  override def decay(coefficient: Double, iteration: Int, maxIterations: Int): Double = ???

}
