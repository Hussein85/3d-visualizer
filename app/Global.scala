import play.api._

import scala.slick.driver.PostgresDriver.simple._
import play.api.mvc._
import play.api.i18n._
import models.Tag

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}