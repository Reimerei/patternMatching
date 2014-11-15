package game

import org.scalajs.spickling._
import shared._

import scala.collection.immutable.HashMap.HashTrieMap
import scala.collection.immutable.HashSet.HashTrieSet
import scala.collection.immutable.Map.{Map1, Map2, Map3, Map4}
import scala.collection.immutable.Set.{Set1, Set2, Set3, Set4}

/**
 * Support for pickling maps and sets. See https://github.com/jlc/scala-js-pickling/commit/9d97943eb323925108eb236fd383972bca2bd397
 */
object Pickles {
  implicit object MapPickler extends BaseMapPickler[Map[Any, Any]]
  implicit object Map1ickler extends BaseMapPickler[Map1[Any, Any]]
  implicit object Map2ickler extends BaseMapPickler[Map2[Any, Any]]
  implicit object Map3ickler extends BaseMapPickler[Map3[Any, Any]]
  implicit object Map4ickler extends BaseMapPickler[Map4[Any, Any]]
  implicit object HashTrieMapPickler extends BaseMapPickler[HashTrieMap[Any, Any]]

  trait BaseMapPickler[A <: Map[Any, Any]] extends Pickler[A] {
    def pickle[P](x: A)(implicit registry: PicklerRegistry, builder: PBuilder[P]): P = {
      builder.makeArray(x.map {
        case (key, value) =>
          builder.makeObject(
            ("k", registry.pickle(key)),
            ("v", registry.pickle(value))
          )
      }.toSeq: _*)
    }
  }

  trait BaseMapUnpickler[A <: Map[Any, Any]] extends Unpickler[A] {
    def unpickle[P](pickle: P)(implicit registry: PicklerRegistry, reader: PReader[P]): A = {
      val len = reader.readArrayLength(pickle)
      Map((0 to len - 1).map { i =>
        val obj = reader.readArrayElem(pickle, i)
        val key = registry.unpickle(reader.readObjectField(obj, "k"))
        val value = registry.unpickle(reader.readObjectField(obj, "v"))
        (key, value)
      }: _*).asInstanceOf[A] // FIXME: to avoid
    }
  }
  implicit object MapUnpickler extends BaseMapUnpickler[Map[Any, Any]]
  implicit object Map1Unpickler extends BaseMapUnpickler[Map1[Any, Any]]
  implicit object Map2Unpickler extends BaseMapUnpickler[Map2[Any, Any]]
  implicit object Map3Unpickler extends BaseMapUnpickler[Map3[Any, Any]]
  implicit object Map4Unpickler extends BaseMapUnpickler[Map4[Any, Any]]
  implicit object HashTrieMapUnpickler extends BaseMapUnpickler[HashTrieMap[Any, Any]]

  trait BaseSetPickler[A <: Set[Any]] extends Pickler[A] {
    def pickle[P](x: A)(implicit registry: PicklerRegistry, builder: PBuilder[P]): P =
      builder.makeArray(x.map { v => registry.pickle(v) }.toSeq: _*)
  }
  implicit object SetPickler extends BaseSetPickler[Set[Any]]
  implicit object Set1Pickler extends BaseSetPickler[Set1[Any]]
  implicit object Set2Pickler extends BaseSetPickler[Set2[Any]]
  implicit object Set3Pickler extends BaseSetPickler[Set3[Any]]
  implicit object Set4Pickler extends BaseSetPickler[Set4[Any]]
  implicit object HashTrieSetPickler extends BaseSetPickler[HashTrieSet[Any]]

  trait BaseSetUnpickler[A <: Set[Any]] extends Unpickler[A] {
    def unpickle[P](pickle: P)(implicit registry: PicklerRegistry, reader: PReader[P]): A = {
      val len = reader.readArrayLength(pickle)
      Set((0 to len - 1).map { i =>
        registry.unpickle(reader.readArrayElem(pickle, i))
      }: _*).asInstanceOf[A] // FIXME: to avoid
    }
  }
  implicit object SetUnpickler extends BaseSetUnpickler[Set[Any]]
  implicit object Set1Unpickler extends BaseSetUnpickler[Set1[Any]]
  implicit object Set2Unpickler extends BaseSetUnpickler[Set2[Any]]
  implicit object Set3Unpickler extends BaseSetUnpickler[Set3[Any]]
  implicit object Set4Unpickler extends BaseSetUnpickler[Set4[Any]]
  implicit object HashTrieSetUnpickler extends BaseSetUnpickler[HashTrieSet[Any]]

  def register() = {
    PicklerRegistry.register[Set[Any]]
    PicklerRegistry.register[Set1[Any]]
    PicklerRegistry.register[Set2[Any]]
    PicklerRegistry.register[Set3[Any]]
    PicklerRegistry.register[Set4[Any]]
    PicklerRegistry.register[HashTrieSet[Any]]

    PicklerRegistry.register[Map[Any, Any]]
    PicklerRegistry.register[Map1[Any, Any]]
    PicklerRegistry.register[Map2[Any, Any]]
    PicklerRegistry.register[Map3[Any, Any]]
    PicklerRegistry.register[Map4[Any, Any]]
    PicklerRegistry.register[HashTrieMap[Any, Any]]

    PicklerRegistry.register[GameStart]
    PicklerRegistry.register[SetCompleted]
    PicklerRegistry.register(WrongGuess)
    PicklerRegistry.register[GameFinished]
    PicklerRegistry.register[Guess]
    PicklerRegistry.register[JoinGameWithoutId]
    PicklerRegistry.register[JoinGameWithId]
    PicklerRegistry.register[CreateGame]
    PicklerRegistry.register[GameCreated]
    PicklerRegistry.register(GameNotFound)
    PicklerRegistry.register(UserQuit)
    PicklerRegistry.register[OtherUserQuit]
    PicklerRegistry.register[Card]
    PicklerRegistry.register[Player]
    PicklerRegistry.register[Map[Any, Any]]
    PicklerRegistry.register[::[Any]]
    PicklerRegistry.register(Nil)
  }
}