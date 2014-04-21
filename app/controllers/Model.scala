package controllers

import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.db.slick._
import models.Tag
import models.Tags
import utils.FormHelper.saveFormFile

object Model extends Controller {

  case class Model(name: String, tags: List[Tag])

  val modelForm: Form[Model] = Form(
    mapping(
      "name" -> text,
      "tags" -> text)((name, tags) => Model(name, tags.split(",").map(tag => new Tag(None, tag)).toList))((m: Model) => Some(m.name, m.tags.map(tag => tag.name).mkString(","))))

  def addForm = Action { implicit request =>
    Ok(views.html.model.addForm(modelForm))
  }

  def upload = DBAction(parse.multipartFormData) { implicit request =>
    modelForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.model.addForm(formWithErrors))
      },
      model => {
        saveFormFile(request, "object-file")
        saveFormFile(request, "texture-file")
        model.tags.foreach(tag => {
          Logger.info(s"tag: $tag")
          Tags.insert(tag)
        })
        Ok("Model uploaded")
      })
  }

  def allTags = DBAction { implicit request =>
    /*implicit val jsonWriter = new Writes[(String, String)] {
      def writes(c: (String, String)): JsValue = {
        Json.obj(c._1 -> c._2)
      }
    }
    val tags = Tags.all.map(tag => ("tag_name", tag.name))*/
    Ok(Json.toJson(Tags.all.map(_.name)))
  }

}