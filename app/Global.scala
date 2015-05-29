import play.api._
import scala.slick.driver.PostgresDriver.simple._
import play.api.Play.current
import play.api.db.slick._
import play.api.mvc._
import play.api.i18n._
import models.Tag
import models.Models
import org.joda.time.DateTime
import scala.io.Source
import play.api.libs.Files
import java.io.File
import models.Tags
import models.TagModels
import models.TagModel
import utils.Constants
import models.Organizations
import models.Organization
import java.lang.reflect.Constructor
import securesocial.core.RuntimeEnvironment
import securesocial.core.providers._
import securesocial.core.providers.utils.{ Mailer, PasswordHasher, PasswordValidator }
import securesocial.museum.MyUserService
import models.User
import scala.collection.immutable.ListMap
import securesocial.museum.MyUserService
import securesocial.controllers.ViewTemplates
import play.api.data.Form
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import play.twirl.api.{ Html, Txt }
import securesocial.core.{ BasicProfile, RuntimeEnvironment }
import securesocial.controllers.MailTemplates

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")
    Insert.inactiveUsersOrganization
  }

  /**
   * Demo application's custom Runtime Environment
   */
  object DemoRuntimeEnvironment extends RuntimeEnvironment.Default[User] {
    override lazy val userService: MyUserService = new MyUserService
    override lazy val providers = ListMap(
      include(new UsernamePasswordProvider[User](userService, avatarService, viewTemplates, passwordHashers)))
    override lazy val mailTemplates: MailTemplates = new MailTemplates.Default(this)
  }

  /**
   * Dependency injection on Controllers using Cake Pattern
   *
   * @param controllerClass
   * @tparam A
   * @return
   */
  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    val instance = controllerClass.getConstructors.find { c =>
      val params = c.getParameterTypes
      params.length == 1 && params(0) == classOf[RuntimeEnvironment[User]]
    }.map {
      _.asInstanceOf[Constructor[A]].newInstance(DemoRuntimeEnvironment)
    }
    instance.getOrElse(super.getControllerInstance(controllerClass))
  }

  object Insert {

    def inactiveUsersOrganization() = {
      DB.withSession { implicit session =>
        if (Organizations.get(1).isEmpty) {
          Organizations.insert(new Organization(Some(1), "Inactive Users"))
        }
      }
    }
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}