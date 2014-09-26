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
import securesocial.core.{ Identity, Authorization }
import securesocial.museum.Normal
import securesocial.museum.Contributer
import models.Tables
import models.Users
import models.Organizations
import models.Organization
import play.api.libs.json._
import models.User

object Admin extends Controller with securesocial.core.SecureSocial {

  def index = SecuredAction(securesocial.museum.Admin) { implicit request =>
    DB.withSession { implicit session =>
      val users = TableQuery[Users]
      val organizations = TableQuery[Organizations]

      val usersWithOrganizationQuery = for {
        user <- users
        organization <- organizations if user.organizationId === organization.id
      } yield (user, organization)

      Ok(views.html.admin(usersWithOrganizationQuery.list))
    }
  }

  def users = SecuredAction(securesocial.museum.Admin) { implicit request =>
    DB.withSession { implicit session =>

      val users = TableQuery[Users]
      val organizations = TableQuery[Organizations]

      val usersWithOrganizationQuery = for {
        user <- users
        organization <- organizations if user.organizationId === organization.id
      } yield (user, organization)

      implicit val locationWrites = new Writes[(User, Organization)] {
        def writes(userOrganization: (User, Organization)) = {
          val user = userOrganization._1
          val organization = userOrganization._2
          Json.obj(
            "firstName" -> user.firstName,
            "email" -> user.email,
            "fullName" -> user.fullName,
            "organization" -> organization.name,
            "organizationId" -> organization.id,
            "lastName" -> user.lastName)
        }
      }

      Ok(Json.toJson(usersWithOrganizationQuery.list))

    }
  }

}