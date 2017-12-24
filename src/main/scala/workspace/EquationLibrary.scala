package workspace

object EquationLibrary {
  import Dimension.{Joule,Newton}

  val library: Map[String, Equation] = Map(
    "ke_def" -> Equation("Definition of kinetic energy",
      Expression(0.5, Map("KE" -> -1, "m" -> 1, "v" -> 2)),
      Map("KE" -> Joule, "m" -> Kilogram, "v" -> Meter / Second),
      Map("KE" -> "Kinetic energy", "m" -> "Mass", "v" -> "Velocity")),
    "pe_def" -> Equation("Definition of gravitational potential energy", Expression(1, Map("PE" -> -1, "m" -> 1, "g" -> 1, "h" -> 1)),
      Map("PE" -> Joule, "m" -> Kilogram, "g" -> Meter / Second ** 2, "h" -> Meter),
      Map("PE" -> "Potential energy", "m" -> "Mass", "g" -> "Strength of gravity", "h" -> "Height"),
    )
  )
  // unsafe
  def getByEqId(name: String): Equation = library(name)
}
