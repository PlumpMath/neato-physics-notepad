package workspace

import cas.RationalNumber

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.util.Try


trait SiDimension {

  val units: Map[SiUnit, RationalNumber[String]]

  def *(other: SiDimension): SiDimension = SiDimension(
    (units.keys ++ other.units.keys)
      .map((u) => u -> (this.units.getOrElse(u, RationalNumber[String](0)) + other.units.getOrElse(u, RationalNumber[String](0))).asInstanceOf[RationalNumber[String]])
      .filter(_._2 != RationalNumber(0))
      .toMap
  )

  def /(other: SiDimension): SiDimension = this * (other ** -1)
  def **(other: Int): SiDimension = SiDimension(this.units.mapValues(x => (x * other).asInstanceOf[RationalNumber[String]]))
  def **(other: RationalNumber[String]): SiDimension = SiDimension(this.units.mapValues(n => (n * other).asInstanceOf[RationalNumber[String]]))

  @JSExport
  lazy val toBuckTex: BuckTex = this match {
    case SiDimension.SiJoule => Text("J")
    case SiDimension.SiNewton => Text("N")
    case SiDimension.SiCoulomb => Text("C")
    case SiDimension.SiVolt => Text("V")
    case SiDimension.SiWatt => Text("W")
    case SiDimension.SiOhm => Text("Ω")
    case SiDimension.SiNewtonsPerCoulomb => Text("N/C")
    case _ => CompileToBuckTex.horizontalBox(units.toList.sortBy(_._1.symbol).map({
      case (unit, RationalNumber(1, 1)) => Text(unit.symbol)
      case (unit, power) => CompileToBuckTex.horizontalBox(List(Text(" " + unit.symbol), Sup(List(Text(power.toString)))))
    }))
  }

  @JSExport
  def equalUnits(other: SiDimension): Boolean = this.units == other.units
}

case class ConcreteSiDimension(units: Map[SiUnit, RationalNumber[String]]) extends SiDimension


object SiDimension {
  val SiNewton: SiDimension = SiDimension.fromInts(Map[SiUnit, Int](Kilogram -> 1, Meter -> 1, Second -> -2))
  val SiJoule: SiDimension = SiNewton * Meter
  val SiCoulomb: SiDimension = Ampere * Second
  val SiVolt: SiDimension = SiJoule / SiCoulomb
  val SiNewtonsPerCoulomb: SiDimension = SiNewton / SiCoulomb
  val SiWatt: SiDimension = SiJoule / Second
  val SiOhm: SiDimension = SiVolt / Ampere
  val Dimensionless = ConcreteSiDimension(Map())

  def apply(units: Map[SiUnit, RationalNumber[String]]): SiDimension = ConcreteSiDimension(units)
  def fromInts(units: Map[SiUnit, Int]): SiDimension = ConcreteSiDimension(units.mapValues(int => RationalNumber[String](int)))
}

case class Unit(value: Double, dimension: SiDimension, name: UnitName) {
  def toDim: Dimension = Dimension(Map(this -> RationalNumber(1)))
}

case class Dimension(units: Map[Unit, RationalNumber[String]]) {
  def totalConstant: Double = units.map({ case (unit: Unit, power: RationalNumber[String]) => math.pow(unit.value, power.toDouble) }).product

  def siDimension: SiDimension = units.map({ case (unit: Unit, power: RationalNumber[String]) => unit.dimension ** power })
    .reduceOption(_ * _)
    .getOrElse(SiDimension.Dimensionless)

  def *(other: Dimension): Dimension = {
    Dimension((units.keys ++ other.units.keys)
      .map((u) => u ->
        (this.units.getOrElse(u, RationalNumber(0)) + other.units.getOrElse(u, RationalNumber(0))).asInstanceOf[RationalNumber[String]]
      )
      .filter(_._2 != RationalNumber(0))
      .toMap)
  }

  def /(other: Dimension): Dimension = this * (other ** -1)

  def **(other: RationalNumber[String]): Dimension = Dimension(this.units.mapValues(n => (n * other).asInstanceOf[RationalNumber[String]]))
  def **(other: Int): Dimension = Dimension(this.units.mapValues(n => (n * RationalNumber[String](other)).asInstanceOf[RationalNumber[String]]))

  def toBuckTex(value: Double): BuckTex = if (this.units.isEmpty) Text("") else {
    def makeSubscript(num: RationalNumber[String]): Option[BuckTex] = {
      if (num == RationalNumber(1)) None else Some(Sup(List(Text(num.toString))))
    }

    val initialUnitsTex = units.dropRight(1).flatMap({ case (unit: Unit, num: RationalNumber[String]) =>
      List(Text("·"), Text(unit.name.getString(1))) ++ makeSubscript(num).toList
    })

    val (lastUnit, lastUnitPower) = units.last
    val pluralizedLastUnitName = if (lastUnitPower.toDouble > 0) lastUnit.name.getString(value) else lastUnit.name.getString(1)
    val lastUnitsTex: List[BuckTex] = List(Text("·"), Text(pluralizedLastUnitName)) ++ makeSubscript(lastUnitPower).toList

    CompileToBuckTex.horizontalBox((initialUnitsTex ++ lastUnitsTex).tail.toList)
  }

}

@JSExportTopLevel("Gem.Dimension")
object Dimension {
  val Dimensionless: Dimension = Dimension(Map())
  import SiDimension._

  val minute = Unit(60, Second, PluralizingUnitName("minute"))
  val hour = Unit(60 * 60, Second, PluralizingUnitName("hour"))
  val day = Unit(60 * 60 * 24, Second, PluralizingUnitName("day"))
  val week = Unit(60 * 60 * 24 * 7, Second, PluralizingUnitName("week"))
  val month = Unit(60 * 60 * 24 * 30, Second, PluralizingUnitName("month"))
  val year = Unit(60 * 60 * 24 * 365.25, Second, PluralizingUnitName("year"))
  val kilometer = Unit(1000, Meter, SymbolUnitName("km"))
  val centimeter = Unit(0.01, Meter, SymbolUnitName("cm"))
  val millimeter = Unit(0.001, Meter, SymbolUnitName("mm"))
  val nanometer = Unit(1e-9, Meter, SymbolUnitName("nm"))
  val lightYear = Unit(9.4607e15, Meter, PluralizingUnitName("light year"))
  val tonne = Unit(1e3, Kilogram, PluralizingUnitName("tonne"))
  val parsec = Unit(3.08567758e16, Meter, PluralizingUnitName("parsec"))
  val meter = Unit(1, Meter, SymbolUnitName("m"))
  val kilogram = Unit(1, Kilogram, SymbolUnitName("kg"))
  val second = Unit(1, Second, SymbolUnitName("s"))
  val degreeKelvin = Unit(1, Kelvin, SymbolUnitName("°K"))
  val ampere = Unit(1, Ampere, SymbolUnitName("A"))
  val joule = Unit(1, SiJoule, SymbolUnitName("J"))
  val watt = Unit(1, SiWatt, SymbolUnitName("W"))
  val electronVolt = Unit(1.60217662e-19, SiJoule, SymbolUnitName("eV"))
  val newton = Unit(1, SiNewton, SymbolUnitName("N"))
  val hertz = Unit(1, Second ** -1, SymbolUnitName("Hz"))
  val ohm = Unit(1, SiOhm, SymbolUnitName("Ω"))
  val coulomb = Unit(1, SiCoulomb, SymbolUnitName("C"))
  val volt = Unit(1, SiVolt, SymbolUnitName("V"))

  lazy val units: Set[(Set[String], Unit)] = Set(
    Set("min", "mins", "minute", "minutes") -> minute,
    Set("hour", "hours") -> hour,
    Set("day") -> day,
    Set("week") -> week,
    Set("month") -> month,
    Set("year") -> year,
    Set("lightyear", "lightyears") -> lightYear,
    Set("tonne", "tonnes") -> tonne,
    Set("parsec") -> parsec,
    Set("ohm") -> ohm,
  ) ++ (for {
    (name: String, unit: Unit) <- unitsWhichCanHaveSiPrefixes
    (siPrefix: String, multiplier: Double) <- siPrefixes ++ Set("" -> 1)
  } yield (Set(siPrefix + name), Unit(unit.value * multiplier, unit.dimension, SymbolUnitName(siPrefix + name))))

  val unitsWhichCanHaveSiPrefixes: Set[(String, Unit)] = Set(
    "m" -> meter,
    "g" -> Unit(0.001, Kilogram, SymbolUnitName("g")),
    "s" -> second,
    "K" -> degreeKelvin,
    "A" -> ampere,
    "J" -> joule,
    "W" -> watt,
    "eV" -> electronVolt,
    "N" -> newton,
    "Hz" -> hertz,
    "C" -> coulomb,
    "V" -> Unit(1, SiVolt, SymbolUnitName("V"))
  )

  val siPrefixes: Set[(String, Double)] = Set(
    "a" -> 1e-18,
    "f" -> 1e-15,
    "p" -> 1e-12,
    "n" -> 1e-9,
    "μ" -> 1e-6,
    "m" -> 1e-3,
    "c" -> 0.01,
    "k" -> 1e3,
    "M" -> 1e6,
    "G" -> 1e9,
    "T" -> 1e12,
    "P" -> 1e15,
    "E" -> 1e18,
  )

  def parseJs(str: String): Dimension = parse(str).getOrElse(null)

  def parse(str: String): Try[Dimension] = Try({
    val unitRegex = raw"(/)?(\w+)(\^([-\d]+))?".r("divisionSign", "unitString", "unimportantGroup", "exponent")
    unitRegex.findAllMatchIn(str).map({
      case unitRegex(divisionSignOrNull, unitString, _, exponentOrNull) =>
        val exponent = Option(exponentOrNull)
        val divisionSign = Option(divisionSignOrNull)
        Dimension.units.find(_._1.contains(unitString)).map(_._2).getOrElse({
          throw new RuntimeException(s"unknown unit $unitString")
        }).toDim ** (
          exponent.map((x) => RationalNumber[String](x.toInt)).getOrElse(RationalNumber[String](1))
          * (if (divisionSign.isDefined) RationalNumber[String](-1) else RationalNumber[String](1))).asInstanceOf[RationalNumber[String]]
    }).reduce(_ * _)
  })
}

sealed trait SiUnit extends SiDimension {
  val units = Map(this -> RationalNumber(1))

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

trait UnitName {
  def getString(num: Double): String
}
case class SymbolUnitName(symbol: String) extends UnitName {
  def getString(x: Double): String = symbol
}
case class PluralizingUnitName(name: String) extends UnitName {
  def getString(x: Double): String = if (x == 1.0) name else name + "s"
}
