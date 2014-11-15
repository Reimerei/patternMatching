/**
 * Created by reflektor on 15.11.14.
 */
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import shared._
import models.Game;

import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */

class GameSpec extends Specification {

  "Game" should {

    "validate accepts valid set" in {
      val cards = Seq(Card(Seq(1,1,1)), Card(Seq(2,2,1)), Card(Seq(3,3,1)))
      Game.validate(cards, cards.toSet) must beTrue
    }

    "validateEqualityRule accepts valid set" in {
      Game.validateEqualityRule(Seq(Card(Seq(1,1,1)), Card(Seq(2,2,1)), Card(Seq(3,3,1)))) must beTrue
    }

    "validateEqualityRule rejects invalid set" in {
      Game.validateEqualityRule(Seq(Card(Seq(1, 1, 1)), Card(Seq(1, 1, 1)), Card(Seq(5, 1, 1)))) must beFalse
    }

    "validateDeckPresence accepts present set" in {
      val cards = Seq(Card(Seq(1,1,1)), Card(Seq(2,2,1)), Card(Seq(3,3,1)))
      Game.validateDeckPresence(cards, cards.toSet) must beTrue
    }

    "validateDeckPresence rejects absent set" in {
      val cards = Seq(Card(Seq(1,1,1)), Card(Seq(2,2,1)), Card(Seq(3,3,1)))
      Game.validateDeckPresence(Seq(Card(Seq(6,6,6))), cards.toSet) must beFalse
    }

    "hasMoreSets is true when there is more combinations" in {
      Game.hasMoreSets(Set(Card(Seq(1,1,1)), Card(Seq(2,2,1)), Card(Seq(3,3,1)))) must beTrue
    }
    "hasMoreSets is false when there is no more combinations" in {
      Game.hasMoreSets(Set(Card(Seq(1,1,1)), Card(Seq(2,2,1)))) must beFalse
    }
  }
}
