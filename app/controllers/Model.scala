package controllers

import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.db.slick._
import play.api.data.validation._
import play.api.Play.current
import models.Tag
import models.Tags
import utils.FormHelper.saveFormFile
import java.sql.Date
import org.joda.time.DateTime
import models.Models
import models.TagModels
import models.TagModel
import scala.slick.driver.PostgresDriver.simple._
import securesocial.museum.UserService
import models.Tag
import securesocial.museum.Normal
import securesocial.museum.Contributer

object Model extends Controller with securesocial.core.SecureSocial {

  case class Model(name: String, material: String, location: String, text: String, year: Int, tags: List[Tag])

  def all = SecuredAction(Normal) { implicit request =>
    val models = DB.withSession { implicit session => Models.all }
    val modelsTags = models.map { model =>
      val tags = DB.withSession { implicit session => Tags.tags(model) }
      (model, tags)
    }
    Ok(views.html.model.browser(modelsTags))
  }

  def thumbnail(id: Int) = SecuredAction(Normal) { implicit request =>
    DB.withSession { implicit session =>
      Models.get(id) match {
        case None => NotFound("The requested model is either not in the db or you lack access to it.")
        case Some(model) => {
          Ok(views.html.model.thumbnail(model, Tags.tags(model)))
        }
      }
    }
  }

  val formObject = "object-file"
  val textureObject = "texture-file"
  val thumbnailObject = "thumbnail-file"

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

  def addForm = SecuredAction(Contributer) { implicit request =>
    Ok(views.html.model.addForm(modelForm))
  }

  def upload = SecuredAction(Contributer)(parse.multipartFormData) { implicit request =>
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
        case head :: tail => addFileMissingErrorsToForm(form.withError(head._1, head._2), tail)
      }
    }
    modelForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.model.addForm(addFileMissingErrorsToForm(formWithErrors, filesMissing)))
      },
      m => {
        val userId: String = request.user.identityId.userId
        val dbModel = new models.Model(id = None, name = m.name, userID = userId, date = new DateTime(m.year, 1, 1, 0, 0, 0),
          material = m.material, location = m.location, text = m.text,
          pathObject = saveFormFile(request, formObject),
          pathTexure = saveFormFile(request, textureObject),
          pathThumbnail = saveFormFile(request, thumbnailObject))
        Logger.info(s"model: $dbModel")
        val modelID = DB.withSession { implicit session => Models.insert(dbModel) };
        Logger.info(s"Modellinfo: $modelID")
        m.tags.foreach(tag => {
          Logger.info(s"tag: $tag")
          val tagID = DB.withSession { implicit session => Tags.insert(tag) }
          DB.withSession { implicit session => TagModels.insert(TagModel(tagID, modelID)) }
        })
        Redirect(routes.Model.all)
      })
  }

  def allTags = SecuredAction(Normal) { implicit request =>
    Ok(Json.toJson(DB.withSession { implicit session => Tags.all.map(_.name) }))
  }

}