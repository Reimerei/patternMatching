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
      (new Game(5)).validate(Set(new Card(Seq(1,1,1)), new Card(Seq(2,2,1)), new Card(Seq(3,3,1)))) must beTrue
    }
  }
}
