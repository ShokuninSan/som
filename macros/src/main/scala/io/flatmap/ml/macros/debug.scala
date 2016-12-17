package io.flatmap.ml.macros

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

object debug {

  def apply[T](x: => T): T = macro impl

  def impl(c: Context)(x: c.Tree) = { import c.universe._
    val q"..$stats" = x
    val loggedStats = stats.flatMap { stat =>
      val msg = "executing " + stat
      List(q"println($msg)", stat)
    }
    q"..$loggedStats"
  }

}
