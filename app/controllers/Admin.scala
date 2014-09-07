package controllers

import play.api._
import play.api.mvc._
import utils.Language
import utils.Constants
import models.Models
import play.api.db.slick.DBAction
import play.api.db.slick._
import play.api.db.DB._
import models.Tags
import scala.slick.driver.PostgresDriver.simple._
import scala.slick.driver.PostgresDriver.simple.{ Session => SlickSession }
import play.api._
import play.api.Play.current
import securesocial.core.{Identity, Authorization}
import securesocial.museum.Normal
import securesocial.museum.Contributer

object Admin extends Controller with securesocial.core.SecureSocial {
  
 
  def index = SecuredAction(securesocial.museum.Admin) { implicit request =>

     Ok(views.html.admin())

  }
  
}