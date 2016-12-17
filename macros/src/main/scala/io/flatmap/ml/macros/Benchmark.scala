package io.flatmap.ml.macros

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.meta._

@compileTimeOnly("@Benchmark annotation not expanded")
class Benchmark extends StaticAnnotation {

  inline def apply(defn: Any): Any = meta {
    defn match {
      case q"..$mods def $name[..$tparams](...$argss): $tpeopt = $expr" =>
        println("Parameters: " + argss)
        q"""
          ..$mods def $name[..$tparams](...$argss): $tpeopt = {
           val start = System.nanoTime()
           val result = $expr
           val end = System.nanoTime()
           println(${name.toString} + " elapsed time: " + (end - start) + "ns")
           result
          }
        """
      case _ => abort("@Benchmark annotation works only on methods")
    }
  }

}
