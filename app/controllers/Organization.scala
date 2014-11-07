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
import securesocial.museum._
import models._
import play.api.libs.json._
import models.User
import utils.CacheWrapper
import models.notInDB.Role
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.User
import play.api.data._
import play.api.data.Forms._

object OrganizationController extends Controller with securesocial.core.SecureSocial{
  
  case class OrganizationFormModel(name: String)
  
  val organizationForm: Form[OrganizationFormModel] = Form(
    mapping(
      "name" -> nonEmptyText)(
        (name) => OrganizationFormModel(name))(o => Some(o.name)))

  
  def newOrganization = SecuredAction(securesocial.museum.Admin)(parse.multipartFormData) { implicit request =>
    DB.withSession { implicit session =>
      organizationForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(formWithErrors.errorsAsJson)
      },
      o => {
        Ok(s"Recieved but not saved the following Organization: $o")
      })
    }
  }
  
  def organizations = SecuredAction(securesocial.museum.Admin) { implicit request =>
    DB.withSession { implicit session =>

      val organizations = TableQuery[Organizations]

      val organizationsQuery = for {
        organization <- organizations
      } yield organization

      implicit val organizationWrites = Json.writes[Organization]

      Ok(Json.toJson(organizationsQuery.list))

    }
  }
  

}