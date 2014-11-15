package models

trait EloRating {

  val K: Double = 32
  val D: Double = 400

  def expected: (Int, Int) => Double = (r1, r2) =>
    1.0 / (1.0 + math.pow(10, (r2.toDouble - r1.toDouble) / D))

  def updatedRating: (Int, Int, Double) => Int =
    (r1, r2, score) =>
      (r1 + K * (score - expected(r1, r2))).toInt

  def scoreTwo: (Int, Int) => (Double, Double) = (p1, p2) =>
    if (p1 == p2)
      (0.5, 0.5)
    else if (p1 > p2)
      (1.0, 0.0)
    else
      (0.0, 1.0)


  def rateGame: List[(String, Int, Int)] = ???
}
