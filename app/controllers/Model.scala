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
import java.sql.Date
import org.joda.time.DateTime
import models.Models
import models.TagModels
import models.TagModel

object Model extends Controller {

  case class Model(name: String, material: String, location: String, text: String, year: Int, tags: List[Tag])

  val modelForm: Form[Model] = Form(
    mapping(
      "name" -> nonEmptyText,
      "material" -> text,
      "location" -> text,
      "text" -> text,
      "year" -> number,
      "tags" -> nonEmptyText)(
        (name, material, location, text, year, tags) => Model(name, material, location, text, year, tags.split(",").map(tag => new Tag(None, tag)).toList))((m: Model) => Some(m.name, m.material, m.location, m.text, m.year, m.tags.map(tag => tag.name).mkString(","))))

  def addForm = Action { implicit request =>
    Ok(views.html.model.addForm(modelForm))
  }

  def upload = DBAction(parse.multipartFormData) { implicit request =>
    modelForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.model.addForm(formWithErrors))
      },
      m => {
        // TODO: Take userID from session and real paths for object and texture
        val dbModel = new models.Model(id = None, name = m.name, userID = 1, date = new DateTime(m.year, 1, 1, 0, 0, 0),
          material = m.material, location = m.location, text = m.text,
          pathObject = saveFormFile(request, "object-file"), pathTexure = saveFormFile(request, "texture-file"))
        Logger.info(s"model: $dbModel")
        val modelID = Models.insert(dbModel);
        Logger.info(s"Modellinfo: $modelID")
        m.tags.foreach(tag => {
          Logger.info(s"tag: $tag")
          val tagID = Tags.insert(tag)
          TagModels.insert(TagModel(tagID, modelID))
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