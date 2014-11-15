import pickles.Pickles
import play.api._
import play.api.mvc._
import play.filters.csrf._

object Global extends WithFilters(CSRFFilter()) with GlobalSettings {

  override def onStart(app: Application): Unit = {
    Pickles.register
  }

}
