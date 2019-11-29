package volga


import volga.syntax.comp._

final abstract class Combo[+A, +B]
case object Dummy
sealed trait Trace[-A, +B]

case class Named(name: String)                                            extends Trace[Any, Nothing]
case class Split[-A, +B, -C, +D](first: Trace[A, B], second: Trace[C, D]) extends Trace[Combo[A, C], Combo[B, D]]
case class Continue[-A, B, +C](begin: Trace[A, B], continue: Trace[B, C]) extends Trace[A, C]

object Trace {
  implicit object symon extends Symon[Trace, Combo, Dummy.type] {
    def swap[A, B]: Trace[Combo[A, B], Combo[B, A]] = Named("swap")

    def lunit[A]: Trace[Combo[Dummy.type, A], A] = Named("eat")

    def unitl[A]: Trace[A, Combo[Dummy.type, A]] = Named("spawn")

    def assocl[A, B, C]: Trace[Combo[A, Combo[B, C]], Combo[Combo[A, B], C]] = Named("assoc")

    def tensor[A, B, C, D](f: Trace[A, B], g: Trace[C, D]): Trace[Combo[A, C], Combo[B, D]] = Split(f, g)

    def id[A]: Trace[A, A] = Named("identity")

    def compose[A, B, C](f: Trace[B, C], g: Trace[A, B]): Trace[A, C] = Continue(g, f)
  }
}

object Nekote  extends App{
  val intToString: Trace[Int, String]       = Named("intToString")
  val stringToDouble: Trace[String, Double] = Named("stringToDouble")
  val concat: Trace[String Combo String, String] = Named("concat")

  val trace = symon[Trace, Combo, Dummy.type]

  val test1: Trace[Int Combo Int, Double] = trace { (a: V[Int], b: V[Int]) =>
    val x = intToString(a)
    val y = intToString(b)
    val r = concat(x, y)

    stringToDouble(r)
  }
}