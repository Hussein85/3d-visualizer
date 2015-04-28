package controllers

import play.api._
import play.api.mvc._
import utils.Language
import utils.Constants
import models._
import play.api.db.slick.DBAction
import play.api.db.slick._
import play.api.db.DB._
import models.Tags
import scala.slick.driver.PostgresDriver.simple._
import scala.slick.driver.PostgresDriver.simple.{ Session => SlickSession }
import play.api._
import play.api.Play.current
import securesocial.core.{ Authorization }
import securesocial.museum.Normal
import securesocial.museum.Contributer
import securesocial.core.RuntimeEnvironment

class Application(override implicit val env: RuntimeEnvironment[User])
  extends securesocial.core.SecureSocial[User] {

  def index = SecuredAction(Normal) { implicit request =>
    Ok.sendFile(Play.getFile("protected/index.html"), inline = true)
  }

  def language = SecuredAction(Normal)(parse.urlFormEncoded) { implicit request =>
    val code = request.body.get("code").head.head
    Logger.info(s"Langugage set to $code")
    Ok(s"Language code set to: $code").withSession(
      "languageCode" -> code)
  }

  def javascriptRoutes = SecuredAction(Normal) { implicit request =>
    import routes.javascript._
    Ok(
      Routes.javascriptRouter("jsRoutes")(
        routes.javascript.Application.language,
        routes.javascript.Model.allTags)).as("text/javascript")
  }

  def getUploadedFile(file: String) = SecuredAction(Normal) {
    Ok.sendFile(new java.io.File(Constants.uploadDir, file))
  }
  
  // TODO refactor to use normal assets controller that may set all headers etc.
  def securedAsset(file: String) = SecuredAction(Normal) {
    Ok.sendFile(Play.getFile("protected/" + file), inline = true)
  }

}