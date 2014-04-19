package controllers

import play.api._
import play.api.mvc._
import play.api.libs.Files._
import play.api.data._
import play.api.data.Forms._
import play.api.db.slick._
import utils._
import models.Tag
import models.Tags

object Model extends Controller {

  case class Model(name: String, tags: List[Tag])

  val modelForm: Form[Model] = Form(
    mapping(
      "name" -> text,
      "tags" -> text)((name, tags) => Model(name, tags.split(",").map(tag => new Tag(None, tag)).toList))((m: Model) => Some(m.name, m.tags.map(tag => tag.name).mkString(","))))

  /*"tags" -> list(
      mapping(
        "id" -> optional(number),
        "tags" -> text
        )(Tag.apply)(Tag.unapply))*/

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
        Logger.info(model.toString)
        model.tags.foreach(tag => {
          Logger.info(s"tag: $tag")
          Tags.insert(tag) 
        })
        Ok("Model uploaded")
      })

  }

  def saveFormFile(request: Request[MultipartFormData[TemporaryFile]], requestName: String) = {
    request.body.file(requestName).map { objectFile =>
      import java.io.File
      val filename = objectFile.filename
      val contentType = objectFile.contentType
      val uploadPath = Constants.uploadDir.getPath + s"/$filename"
      objectFile.ref.moveTo(new File(uploadPath), true)
      Logger.info(s"Saved file to: $uploadPath")
    }.getOrElse {
      Logger.info(s"Missing object file: $requestName")
    }
  }

}