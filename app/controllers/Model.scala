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
import utils.FormHelper.saveFormFileToS3
import java.sql.Date
import org.joda.time.DateTime
import models._
import models.TagModels
import models.TagModel
import scala.slick.driver.PostgresDriver.simple._
import models.Tag
import securesocial.museum.Normal
import securesocial.museum.Contributer
import securesocial.core.RuntimeEnvironment

case class FormModel(name: String, material: String, location: String, text: String, year: Int, tags: List[Tag])

object Model {
  
   val fourDigitYearConstraint: Constraint[Int] = Constraint("constraints.4digityear") {
    case i if i > DateTime.now.year.get => Invalid("error.inFuture")
    case i if i.toString.length == 4 => Valid
    case _ => Invalid("error.4digityear")
  }
  val fourDigitYearCheck: Mapping[Int] = number.verifying(fourDigitYearConstraint)
  
  def modelForm: Form[FormModel] = Form(
    mapping(
      "name" -> nonEmptyText,
      "material" -> text,
      "location" -> text,
      "text" -> text,
      "year" -> fourDigitYearCheck,
      "tags" -> nonEmptyText)(
        (name, material, location, text, year, tags) => FormModel(name, material, location, text, year, tags.split(",").map(tag => new Tag(None, tag)).toList))((m: FormModel) => Some(m.name, m.material, m.location, m.text, m.year, m.tags.map(tag => tag.name).mkString(","))))

}

class Model(override implicit val env: RuntimeEnvironment[User])
  extends securesocial.core.SecureSocial[User]{

  
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

 

  
  def addForm = SecuredAction(Contributer) { implicit request =>
    Ok(views.html.model.addForm(Model.modelForm))
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
    def addFileMissingErrorsToForm(form: play.api.data.Form[FormModel], filesMissing: List[(String, String)]): play.api.data.Form[FormModel] = {
      filesMissing match {
        case Nil => form
        case head :: tail => addFileMissingErrorsToForm(form.withError(head._1, head._2), tail)
      }
    }
    Model.modelForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.model.addForm(addFileMissingErrorsToForm(formWithErrors, filesMissing)))
      },
      m => {
        val userId: String = request.user.userId
        val dbModel = new models.Model(id = None, name = m.name, userID = userId, date = new DateTime(m.year, 1, 1, 0, 0, 0),
          material = m.material, location = m.location, text = m.text,
          pathObject = saveFormFileToS3(request, formObject),
          pathTexure = saveFormFileToS3(request, textureObject),
          pathThumbnail = saveFormFileToS3(request, thumbnailObject))
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