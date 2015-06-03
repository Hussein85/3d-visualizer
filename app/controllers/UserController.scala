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
import securesocial.core.{ Authorization }
import securesocial.museum._
import models._
import play.api.libs.json._
import utils.CacheWrapper
import models.notInDB.Role
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.User
import play.api.data._
import play.api.data.Forms._
import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.core.util.ScalaJsonUtil
import com.wordnik.swagger.annotations.ApiOperation
import com.wordnik.swagger.annotations.ApiImplicitParams
import com.wordnik.swagger.annotations._
import securesocial.core.RuntimeEnvironment

class UserController(override implicit val env: RuntimeEnvironment[User])
  extends securesocial.core.SecureSocial[User] {

   def loggedIn = SecuredAction(Normal) { implicit request =>
    Ok(Json.toJson(request.user.role.toString()))
  }

}