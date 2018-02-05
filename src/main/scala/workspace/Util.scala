package workspace
import scala.annotation._
import scala.collection.immutable
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel, ScalaJSDefined}


object Util {
  def traceShow[A](a: A): A = {
    println(a)
    a
  }

  def err[A](s: String): A = throw new RuntimeException(s)

  def annotateFailWithMessage[A](string: String, f: => A): A = {
    try {
      f
    } catch {
      case e: RuntimeException => {
        println(string)
        throw e
      }
    }
  }

  def showNumber(value: Double): String = {
//    if (value == 0) {
//      "0"
//    } else {
//      val exponent = math.log10(value).floor
//      if (exponent > 4) {
//        val significand = value / math.pow(10, exponent)
//        val significandDigits = (significand * math.pow(10, exponent)).round.toString.toList
//
//        "%.4g".format(value)
//        // Scientific notation. Round to 4 sig figs, eliding trailing zeros.
//      } else if (exponent > 0) {
//
//      }
//    }
    val roundingFactor = math.pow(10, math.log10(value).floor)
    val roundedNum = (value / roundingFactor * 100000).round * roundingFactor / 100000
    val string = roundedNum.toString

    def fixUpString(inString: String): String = {
      if (inString.take(6).contains('.')) {
        val shortenedStr = inString.take(6).reverse.dropWhile(_ == '0').reverse
        if (shortenedStr.last == '.') {
          shortenedStr.dropRight(1)
        } else {
          shortenedStr
        }
      } else {
        inString
      }
    }

    if (string.contains('E')) {
      val Array(multiplier, exponent) = string.split('E')
      s"${fixUpString(multiplier)}e$exponent"
    } else if (math.abs(value) > 0.000001) {
      fixUpString(string)
    } else {
      string
    }

  }

  def main(args: Array[String]): Unit = {
    println(showNumber(1.123123))
    println(showNumber(1.12312))
    println(showNumber(1.1231))
    println(showNumber(1.123))
    println(showNumber(1.12))
    println(showNumber(1.1))
    println(showNumber(1))
    println(showNumber(1.1200000004))
    println(showNumber(11200.000004))
    println("hah!")
    println(showNumber(0.434))
    println(showNumber(0.0434))
    println(showNumber(0.00434))
    println(showNumber(0.000434))
    println(showNumber(0.0000434))
    println(showNumber(0.00000434))
    println(showNumber(0.000000434))
    println(showNumber(0.0000000434))
  }
}

case class MapWithIds[A](map: Map[Int, A], nextId: Int = 0) {
  type Pair = (Int, A)
  def get(key: Int): Option[A] = map.get(key)
  def apply(key: Int): A = map(key)
  def getOrElse(key: Int, other: A): A = map.getOrElse(key, other)
  def keySet: Set[Int] = map.keySet
  def keys: Iterable[Int] = map.keys
  def values: Iterable[A] = map.values
  def find(p: Pair => Boolean): Option[Pair] = map.find(p)
  def addWithNextId(thing: A): MapWithIds[A] = MapWithIds(map + (nextId -> thing), nextId + 1)
  def toSet: Set[Pair] = map.toSet
  def set(k: Int, v: A): MapWithIds[A] = this.copy(map + (k -> v))
  def delete(k: Int): MapWithIds[A] = this.copy(map - k)
  def mapValues[B](f: A => B): MapWithIds[B] = this.copy(map = map.mapValues(f))
}

object MapWithIds {
  def empty[A]: MapWithIds[A] = MapWithIds[A](Map(), 0)
  def fromMap[A](map: Map[Int, A]): MapWithIds[A] = MapWithIds(map, if (map.isEmpty) 0 else map.keys.max + 1)
}


case class TestClass(foo: Int, bar: Option[String]) {
  def thing: String = s"$foo $bar"
}


trait TestClassJs extends js.Object {
  val foo: Int
  val bar: js.UndefOr[String]
}

@JSExportTopLevel("Gem.Test")
object TestClassJs {
  implicit class TestClassOps(val self: TestClassJs) extends AnyVal {
    def toTestClass: TestClass = {
      TestClass(self.foo, self.bar.toOption)
    }
  }
  @JSExport("foo")
  def foo(x: TestClassJs): String = x.toTestClass.thing
}


