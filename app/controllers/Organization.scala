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
import com.wordnik.swagger.annotations.Api
import com.wordnik.swagger.core.util.ScalaJsonUtil
import com.wordnik.swagger.annotations.ApiOperation
import com.wordnik.swagger.annotations.ApiImplicitParams
import com.wordnik.swagger.annotations._

@Api(value = "/organization", description = "Operations about organizations")
object OrganizationController extends Controller with securesocial.core.SecureSocial {

  case class OrganizationFormModel(name: String)

  val organizationForm: Form[Organization] = Form(
    mapping(
      "name" -> nonEmptyText)(
        (name) => Organization(None, name))(o => Some(o.name)))

  @ApiOperation(
      nickname = "New Organization", 
      value = "Add a new organization", 
      notes = "Adds a new organization", 
      response = classOf[OrganizationFormModel], 
      httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(value = "Organization object that will be creatd", required = true, dataType = "Organization", paramType = "body")))
  def newOrganization = SecuredAction(securesocial.museum.Admin)(parse.json) { implicit request =>
    DB.withSession { implicit session =>
      organizationForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(formWithErrors.errorsAsJson)
        },
        o => {
          Organizations.insert(o)
          Ok("Saved")
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