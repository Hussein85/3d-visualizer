package controllers

import play.api._
import play.api.libs.json._ // JSON library
import play.api.libs.json.Writes._
import play.api.libs.functional.syntax._ // Combinator syntax
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.db.slick._
import play.api.data.validation._
import play.api.Play.current
import models.Tag
import models.Tags
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
import fly.play.s3._
import fly.play.aws._

case class FormModel(name: String, material: String, location: String, text: String, year: Int, tags: List[Tag])

object Model {

  val fourDigitYearConstraint: Constraint[Int] = Constraint("constraints.4digityear") {
    case i if i > DateTime.now.year.get => Invalid("error.inFuture")
    case i if i.toString.length == 4    => Valid
    case _                              => Invalid("error.4digityear")
  }
  val fourDigitYearCheck: Mapping[Int] = number.verifying(fourDigitYearConstraint)

  //TODO: Refactor to use JSON read instead of FORM
  
  def modelForm: Form[FormModel] = Form(
    mapping(
      "name" -> nonEmptyText,
      "material" -> text,
      "location" -> text,
      "text" -> text,
      "year" -> fourDigitYearCheck,
      "tags" -> play.api.data.Forms.list(mapping(
        "text" -> text)((text) =>
          new Tag(None, text))((tag: Tag) => Some(tag.name))))(FormModel.apply)(FormModel.unapply))

}

class Model(override implicit val env: RuntimeEnvironment[User])
  extends securesocial.core.SecureSocial[User] {
  
  def all = SecuredAction(Normal) { implicit request =>
    val models = DB.withSession { implicit session => Models.all }
    Ok(Json.toJson(models))
  }

  val formObject = "object-file"
  val textureObject = "texture-file"
  val thumbnailObject = "thumbnail-file"

  implicit val modelWrites: Writes[models.Model] = (
    (JsPath \ "id").write[Option[Int]] and
    (JsPath \ "name").write[String] and
    (JsPath \ "userId").write[String] and
    (JsPath \ "date").write[DateTime] and
    (JsPath \ "material").write[String] and
    (JsPath \ "location").write[String] and
    (JsPath \ "text").write[String] and
    (JsPath \ "f1").write[Option[String]] and
    (JsPath \ "f2").write[Option[String]] and 
    (JsPath \ "f3").write[Option[String]])(unlift(models.Model.unapply))

  def get(id: Int) = SecuredAction(Normal) { implicit request =>
    DB.withSession { implicit session =>
      Models.get(id) match {
        case None => NotFound("The requested model is either not in the db or you lack access to it.")
        case Some(model) => {
          Ok(Json.toJson(model))
        }
      }
    }
  }

  def upload = SecuredAction(Contributer)(parse.json) { implicit request =>
    Model.modelForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errorsAsJson)
      },
      m => {
        val userId: String = request.user.userId
        val expiryTime = 36000
        val bucket = S3("museum-dev")
        val pathObject = java.util.UUID.randomUUID.toString
        val pathTexure = java.util.UUID.randomUUID.toString
        val pathThumbnail = java.util.UUID.randomUUID.toString
        val queryString = Map.empty[String, Seq[String]]
        val urlObject = bucket.putUrl(pathObject, expiryTime)
        val urlTexture = bucket.putUrl(pathTexure, expiryTime)
        val urlThumbnail = bucket.putUrl(pathThumbnail, expiryTime)
        val dbModel = new models.Model(id = None, name = m.name, userID = userId, date = new DateTime(m.year, 1, 1, 0, 0, 0),
          material = m.material, location = m.location, text = m.text,
          pathObject = Some(pathObject),
          pathTexure = Some(pathTexure),
          pathThumbnail = Some(pathThumbnail))
        Logger.info(s"model: $dbModel")
        val modelID = DB.withSession { implicit session => Models.insert(dbModel) };
        Logger.info(s"Modellinfo: $modelID")
        m.tags.foreach(tag => {
          Logger.info(s"tag: $tag")
          val tagID = DB.withSession { implicit session => Tags.insert(tag) }
          DB.withSession { implicit session => TagModels.insert(TagModel(tagID, modelID)) }
        })
        
        val json: JsValue = Json.obj(
            "id" -> modelID,
            "urlObject" -> urlObject,
            "urlTexture" -> urlTexture,
            "urlThumbnail" -> urlThumbnail
        )
        
        Ok(Json.toJson(json))
      })
  }

  def allTags = SecuredAction(Normal) { implicit request =>
    Ok(Json.toJson(DB.withSession { implicit session => Tags.all.map(_.name) }))
  }

  def tags(query: String) = SecuredAction(Normal) { implicit request =>
    Ok(Json.toJson(DB.withSession { implicit session => Tags.tag(query).map(_.name) }))
  }

}