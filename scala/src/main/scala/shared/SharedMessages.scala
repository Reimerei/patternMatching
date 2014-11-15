package shared

object SharedMessages {
  def itWorks = "It works!"
}




case class Player(name: String)
case class Card(id: Seq[Int])

// Client receives
case class GameStart(cards: Set[Card], scoreCard: Map[Player, Int], gameId: Long)
case class SetCompleted(cards: Set[Card], scoreCard: Map[Player, Int])
case object WrongGuess
case class GameFinished(scoreCard: Map[Player, Int])

// Client sends
case class Guess(cards: Set[Card])
case class JoinGame(playerName: String, gameId: Option[Long])
case class CreateGame(playerName: String)