package controllers

import play.api._
import play.api.mvc._
import utils.Language

object Application extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.viewer())
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

}