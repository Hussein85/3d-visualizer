package controllers

import play.api._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.db.slick._
import play.api.data.validation._
import play.api.data.format.Formats._
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
import securesocial.museum._
import securesocial.core.RuntimeEnvironment
import fly.play.s3._
import fly.play.aws._

case class FormModel(name: String,
                     material: String,
                     location: String,
                     latitude: Double,
                     longitude: Double,
                     text: String,
                     year: Int,
                     tags: List[Tag])

object ModelController {

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
      "latitude" -> of(doubleFormat),
      "longitude" -> of(doubleFormat),
      "text" -> text,
      "year" -> fourDigitYearCheck,
      "tags" -> play.api.data.Forms.list(mapping(
        "text" -> text)((text) =>
          new Tag(None, text))((tag: Tag) => Some(tag.name))))(FormModel.apply)(FormModel.unapply))

}

class ModelController(override implicit val env: RuntimeEnvironment[User])
    extends securesocial.core.SecureSocial[User] {

  def getPublished = SecuredAction(Normal) { implicit request =>
    val models = DB.withSession { implicit session => Models.published(request.user.organizationId) }
    Ok(Json.toJson(models))
  }

  private case class Published(published: Boolean)

  def setPublished(id: Int) = SecuredAction(Admin)(parse.json) { implicit request =>
    implicit val publishedReads = Json.reads[Published]
    val publishedResult = request.body.validate[Published]
    publishedResult.fold(
      errors => {
        BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(errors)))
      },
      published => {
        DB.withSession { implicit session => Models.setPublished(id, published.published) }
        NoContent
      })
  }

  def unpublished = SecuredAction(Admin) { implicit request =>
    val models = DB.withSession { implicit session => Models.unpublished }
    Ok(Json.toJson(models))
  }

  def get(id: Int) = SecuredAction(Normal) { implicit request =>
    DB.withSession { implicit session =>
      Models.get(id, request.user.organizationId) match {
        case None => NotFound("The requested model is either not in the db or you lack access to it.")
        case Some(model) => {
          Ok(Json.toJson(model))
        }
      }
    }
  }

  def upload = SecuredAction(Contributer)(parse.json) { implicit request =>
    ModelController.modelForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errorsAsJson)
      },
      m => {
        val userId: String = request.user.userId
        val queryString = Map.empty[String, Seq[String]]
        val dbModel = new models.Model(id = None, name = m.name, userID = userId, date = new DateTime(m.year, 1, 1, 0, 0, 0),
          material = m.material, location = m.location, latitude = m.latitude, longitude = m.longitude, text = m.text, timestamp = new DateTime, published = false,
          organizationId = request.user.organizationId)
        Logger.info(s"model: $dbModel")
        val modelID = DB.withSession { implicit session => Models.insert(dbModel) };
        Logger.info(s"Modellinfo: $modelID")
        m.tags.foreach(tag => {
          Logger.info(s"tag: $tag")
          val tagID = DB.withSession { implicit session => Tags.insert(tag) }
          DB.withSession { implicit session =>
            TagModels.insert(TagModel(tagID, modelID), request.user.organizationId)
          }
        })

        import com.typesafe.plugin._
        val mail = use[MailerPlugin].email
        mail.setSubject("A new model has been uploaded.")
        mail.setRecipient("palmqvist.thomas@gmail.com")
        //or use a list
        mail.setFrom("Museumarkiv <museumarkiv@gmail.com>")

        mail.send {
          val model = dbModel.copy(id = Some(modelID))
          s"$model"
        }

        val json: JsValue = Json.obj("id" -> modelID)
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