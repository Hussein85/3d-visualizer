package controllers

import play.api._
import play.api.mvc._
import play.api.libs.Files._
import play.api.data._
import play.api.data.Forms._
import utils._


object Model extends Controller{
  
  case class Model(name: String, age: Int)

  val modelForm = Form(
    mapping(
      "name" -> text,
      "age" -> number)(Model.apply)(Model.unapply))

  def addForm = Action { implicit request =>
    Ok(views.html.model.addForm(modelForm))
  }

  def upload = Action(parse.multipartFormData) { implicit request =>
    modelForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.model.addForm(formWithErrors))
      },
      userData => {
        saveFormFile(request, "object-file")
        saveFormFile(request, "texture-file")
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
      Redirect(routes.Model.addForm).flashing(
        "error" -> "Missing object file")
    }
  }

}