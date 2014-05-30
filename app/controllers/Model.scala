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
import play.api.data.validation._

object Model extends Controller {

  case class Model(name: String, material: String, location: String, text: String, year: Int, tags: List[Tag])
  
  def all = DBAction { implicit request =>
    Ok(views.html.model.browser(Models.all))
  }
  
  def thumbnail(id: Int) = DBAction { implicit request =>
    Models.get(id) match {
      case None => NotFound("The requested model is either not in the db or you lack access to it.")
      case Some(model) => Ok(views.html.model.thumbnail(model))
    }  
  }

  val formObject = "object-file"
  val textureObject = "texture-file"
    
  val fourDigitYearConstraint: Constraint[Int] = Constraint("constraints.4digityear") {
    case i if i > DateTime.now.year.get => Invalid("error.inFuture")
	case i if i.toString.length == 4 => Valid
	case _ => Invalid("error.4digityear")
  }
  val fourDigitYearCheck: Mapping[Int] = number.verifying(fourDigitYearConstraint)

  val modelForm: Form[Model] = Form(
    mapping(
      "name" -> nonEmptyText,
      "material" -> text,
      "location" -> text,
      "text" -> text,
      "year" -> fourDigitYearCheck,
      "tags" -> nonEmptyText)(
        (name, material, location, text, year, tags) => Model(name, material, location, text, year, tags.split(",").map(tag => new Tag(None, tag)).toList))((m: Model) => Some(m.name, m.material, m.location, m.text, m.year, m.tags.map(tag => tag.name).mkString(","))))

  def addForm = Action { implicit request =>
    Ok(views.html.model.addForm(modelForm))
  }

  def upload = DBAction(parse.multipartFormData) { implicit request =>
    val filesMissing: List[(String, String)] =
      ((request.body.file(formObject) match {
        case None => Some(formObject -> "error.required")
        case Some(x) => None
      }) +: (request.body.file(textureObject) match {
        case None => Some(textureObject -> "error.required")
        case Some(x) => None
      }) +: Nil).flatten
    def addFileMissingErrorsToForm(form: play.api.data.Form[Model], filesMissing: List[(String, String)]): play.api.data.Form[Model] = {
      filesMissing match {
        case Nil => form
        case head :: tail => addFileMissingErrorsToForm(form.withError(head._1,head._2), tail)
      }
    }
    modelForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.model.addForm(addFileMissingErrorsToForm(formWithErrors, filesMissing)))
      },
      m => {
        // TODO: Take userID from session
        val dbModel = new models.Model(id = None, name = m.name, userID = 1, date = new DateTime(m.year, 1, 1, 0, 0, 0),
          material = m.material, location = m.location, text = m.text,
          pathObject = saveFormFile(request, formObject), pathTexure = saveFormFile(request, textureObject))
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