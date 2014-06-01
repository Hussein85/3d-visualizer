package controllers

import play.api._
import play.api.mvc._
import utils.Language
import utils.Constants

object Application extends Controller {

  def index = Action { implicit request =>
    Logger.info(play.api.Play.current.configuration.getString("uploadPath").get.replace("~", System.getProperty("user.home")))
    Ok(views.html.viewer())
  }

  def tags = Action { implicit request =>
    Ok(views.html.tags())
  }

  def language = Action(parse.urlFormEncoded) { implicit request =>
    val code = request.body.get("code").head.head
    Logger.info(s"Langugage set to $code")
    Ok(s"Language code set to: $code").withSession(
      "languageCode" -> code)
  }

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(
      Routes.javascriptRouter("jsRoutes")(
        routes.javascript.Application.language,
        routes.javascript.Model.allTags)).as("text/javascript")
  }

  def getUploadedFile(file: String) = Action {
    Ok.sendFile(new java.io.File(Constants.uploadDir, file))
  }

}