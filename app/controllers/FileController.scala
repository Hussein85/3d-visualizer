package controllers

import play.api._
import play.api.libs.json._
import play.api.libs.json.Writes._
import play.api.libs.functional.syntax._
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
import securesocial.museum.Admin
import play.api.libs.json.Json.JsValueWrapper
import com.typesafe.config.ConfigFactory

class FileController(override implicit val env: RuntimeEnvironment[User])
  extends securesocial.core.SecureSocial[User] {

  val bucket = S3(ConfigFactory.load().getString("aws.bucket.name"))
  val expiryTime = 60

  def fileWrites[T](implicit request: SecuredRequest[T]) = new Writes[File] {
    def url(file: File): Seq[(String, JsValue)] = {
      if (Contributer.isAuthorized(request.user, request) ||
        Admin.isAuthorized(request.user, request)) {
        Seq(
          "putUrl" -> JsString(bucket.putUrl(file.id, expiryTime)),
          "getUrl" -> JsString(bucket.url(file.id, expiryTime)))
      } else {
        Seq("getUrl" -> JsString(bucket.url(file.id, expiryTime)))
      }
    }

    def values(file: File): Seq[(String, JsValue)] = {
      val static: Seq[(String, JsValue)] = Seq("id" -> JsString(file.id),
        "modelId" -> JsNumber(file.modelId),
        "type" -> JsString(file.`type`),
        "finished" -> JsBoolean(file.finished))
      static ++ url(file)
    }

    def writes(file: File) = JsObject(values(file))
  }

  case class FilePost(modelId: Int, `type`: String)
  implicit val fileReads: Reads[FilePost] = Json.reads[FilePost]

  def get(id: String) = SecuredAction(Normal) { implicit request =>
    val file = DB.withSession { implicit session => Files.get(id) }
    Ok(Json.toJson(file)(fileWrites))
  }

  def post() = SecuredAction(Normal)(parse.json) { implicit request =>
    request.body.validate[FilePost].fold(
      errors => BadRequest(JsError.toFlatJson(errors)),
      filePost => {
        val file = new File(java.util.UUID.randomUUID.toString, filePost.modelId, filePost.`type`, new DateTime, false, request.user.userId)
        DB.withSession { implicit session => Files.insert(file) }
        Ok(Json.toJson(file)(fileWrites))
      })
  }

  def acc(id: String) = SecuredAction(Normal) { implicit request =>
    DB.withSession { implicit session => Files.setFinished(id) }
    NoContent
  }

  def getModelFiles(modelId: Int) = SecuredAction(Normal) { implicit request =>
    val files = DB.withSession { implicit session => Files.modelFiles(modelId) }
    implicit val fw = fileWrites
    Ok(Json.toJson(files))
  }

}