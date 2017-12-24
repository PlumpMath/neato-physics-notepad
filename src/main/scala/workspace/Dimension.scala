package workspace

trait Dimension {
  val units: Map[SiUnit, Int]

  def *(other: Dimension): Dimension = Dimension(
    (units.keys ++ other.units.keys)
      .map((u) => u -> (this.units.getOrElse(u, 0) + other.units.getOrElse(u, 0)))
      .filter(_._2 != 0)
      .toMap
  )

  def /(other: Dimension): Dimension = Dimension(
    (units.keys ++ other.units.keys)
      .map((u) => u -> (this.units.getOrElse(u, 0) - other.units.getOrElse(u, 0)))
      .filter(_._2 != 0)
      .toMap
  )

  def **(other: Int): Dimension = Dimension(this.units.mapValues(_ * other))
}

case class ConcreteDimension(units: Map[SiUnit, Int]) extends Dimension

object Dimension {
  val Newton = Dimension(Map(Kilogram -> 1, Meter -> 1, Second -> -2))
  val Joule = Newton * Meter

  def apply(units: Map[SiUnit, Int]): Dimension = ConcreteDimension(units)
  def parse(str: String): Dimension = {
    // this accepts args like "kg m^2 s^-2"

    var argList = str.split(' ').toList

    argList.map((arg: String) => {
      if (arg.contains("^")) {
        val splitList = arg.split('^')
        assert(splitList.length == 2, arg)
        symbolMap(splitList(0)) ** splitList(1).toInt
      } else {
        symbolMap(arg)
      }
    }).reduce(_ * _)
  }

  val symbolMap: Map[String, Dimension] = Map(
    "m" -> Meter,
    "kg" -> Kilogram,
    "s" -> Second,
    "K" -> Kelvin,
    "A" -> Ampere,
    "J" -> Joule,
    "N" -> Newton
  )
}

sealed trait SiUnit extends Dimension {
  val units = Map(this -> 1)

  def symbol: String = this match {
    case Meter => "m"
    case Kilogram => "kg"
    case Second => "s"
    case Kelvin => "K"
    case Ampere => "A"
  }
}
case object Meter extends SiUnit
case object Kilogram extends SiUnit
case object Second extends SiUnit
case object Kelvin extends SiUnit
case object Ampere extends SiUnit
