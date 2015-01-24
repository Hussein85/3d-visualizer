package controllers

import play.api._
import play.api.mvc._
import utils.Language
import utils.Constants
import models.Models
import play.api.db.slick.DBAction
import play.api.db.slick._
import play.api.db.DB._
import models.Tags
import scala.slick.driver.PostgresDriver.simple._
import scala.slick.driver.PostgresDriver.simple.{ Session => SlickSession }
import play.api._
import play.api.Play.current
import securesocial.core.{ Authorization }
import securesocial.museum.Normal
import securesocial.museum.Contributer
import models.Tables
import models.Users
import models.Organizations
import models.Organization
import play.api.libs.json._
import models.User
import utils.CacheWrapper
import models.notInDB.Role
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.User
import securesocial.core.RuntimeEnvironment

class Admin(override implicit val env: RuntimeEnvironment[User])
  extends securesocial.core.SecureSocial[User] {

  def index = SecuredAction(securesocial.museum.Admin) { implicit request =>
    Ok(views.html.admin.admin())
  }

  def users = SecuredAction(securesocial.museum.Admin) { implicit request =>
    DB.withSession { implicit session =>

      val users = TableQuery[Users]
      val organizations = TableQuery[Organizations]

      val usersWithOrganizationQuery = for {
        user <- users
        organization <- organizations if user.organizationId === organization.id
      } yield (user, organization)

      implicit val userWrites = new Writes[(User, models.Organization)] {
        def writes(userOrganization: (User, models.Organization)) = {
          val user = userOrganization._1
          val organization = userOrganization._2
          Json.obj(
            "firstName" -> user.firstName,
            "email" -> user.email,
            "fullName" -> user.fullName,
            "organization" -> organization.name,
            "organizationId" -> organization.id,
            "lastName" -> user.lastName,
            "role" -> user.role.toString)
        }
      }

      Ok(Json.toJson(usersWithOrganizationQuery.list))

    }
  }

  def roles = SecuredAction(securesocial.museum.Admin) { implicit request =>
    Ok(Json.arr(Json.obj("name" -> Role.Contributer), Json.obj("name" -> Role.Consumer)))
  }

  case class UpdateUser(organizationId: Int, role: String)

  implicit val locationFormat: Format[UpdateUser] = (
    (JsPath \ "organizationId").format[Int] and
    (JsPath \ "role").format[String])(UpdateUser.apply, unlift(UpdateUser.unapply))

  def updateUser(email: String) = SecuredAction(securesocial.museum.Admin)(BodyParsers.parse.json) { implicit request =>
    val placeResult = request.body.validate[UpdateUser]
    placeResult.fold(
      errors => {
        BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(errors)))
      },
      updateUser => {
        DB.withSession { implicit session =>
          val users = TableQuery[Users]
          val q = for {
            u <- users if u.email === email
          } yield (u.organizationId, u.role)
          q.update(updateUser.organizationId, updateUser.role)

          val statement = q.updateStatement
          val invoker = q.updateInvoker

          Ok(Json.obj("status" -> "OK", "message" -> (s"User $email saved.")))
        }
      })
  }

}