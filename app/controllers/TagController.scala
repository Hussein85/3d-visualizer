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

class TagController(override implicit val env: RuntimeEnvironment[User])
  extends securesocial.core.SecureSocial[User] {

  implicit val TagWrites = new Writes[Tag] {
    def writes(tag: Tag) = Json.obj(
      "text" -> tag.name)
  }

  def tag(modelId: Int) = SecuredAction(Normal) { implicit request =>
    val tags = DB.withSession { implicit session => Tags.tags(modelId) }
    Ok(Json.toJson(tags))
  }

}