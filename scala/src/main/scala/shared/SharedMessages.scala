package shared

object SharedMessages {
  def itWorks = "It works!"
}

case class Player(name: String)
case class Card(id: List[Int])

// Client receives
trait ServerSend
case class GameStart(cards: Set[Card], scoreCard: Map[Player, Int], gameId: Long) extends ServerSend
case class SetCompleted(completedSet: Set[Card], newCards : Set[Card], scoreCard: Map[Player, Int]) extends ServerSend
case object WrongGuess extends ServerSend
case class GameFinished(scoreCard: Map[Player, Int]) extends ServerSend
case class GameCreated(gameId: Long) extends ServerSend
case object GameNotFound extends ServerSend
case class OtherUserQuit(player : Player) extends ServerSend
case class NoCardsLeft(completedSet: Set[Card], scoreCard: Map[Player, Int]) extends ServerSend
case class GameStatus(msg: String) extends ServerSend

// Client sends
trait ClientSends
case class Guess(cards: Set[Card]) extends ClientSends
case class CreateGame(playerName: String) extends ClientSends
trait JoinGame extends ClientSends
case class JoinGameWithoutId(playerName: String) extends JoinGame
case class JoinGameWithId(playerName: String, gameId: Long) extends JoinGame
case object UserQuit extends ClientSends