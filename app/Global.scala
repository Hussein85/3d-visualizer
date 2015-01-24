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
    Insert.exampleModel
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

    def exampleModel() = {
      val exsist = DB.withSession { implicit session => Models.get("static/candleHolder.obj").isDefined }
      if (!exsist) {

        Files.copyFile(Play.getFile("public/3dAssets/candleHolderSmal.jpg"), new File(Constants.uploadDir.getPath + "/static/candleHolderSmal.jpg"), true)
        Files.copyFile(Play.getFile("public/3dAssets/candleHolderSmal.png"), new File(Constants.uploadDir.getPath + "/static/candleHolderSmal.png"), true)
        Files.copyFile(Play.getFile("public/3dAssets/candleHolder.obj"), new File(Constants.uploadDir.getPath + "/static/candleHolder.obj"), true)

        val dbModel = new models.Model(id = Some(1), name = "Exempel", userID = "System", date = new DateTime(1970, 1, 1, 0, 0, 0), material = "Keramaik", location = "Lund", text = "Ett exempel på hur ett föremål kan se ut.", pathObject = "static/candleHolder.obj", pathTexure = "static/candleHolderSmal.jpg", pathThumbnail = "static//candleHolderSmal.png")
        Logger.info(s"model: $dbModel")
        DB.withSession { implicit session =>
          val modelID = Models.insert(dbModel)
          Logger.info(s"Modellinfo: $modelID")
          val tags = List(Tag(None, "Keramik"), Tag(None, "Ljusstake"))
          tags.foreach(tag => {
            Logger.info(s"tag: $tag")

            DB.withSession { implicit session =>
              val tagID = Tags.insert(tag)
              TagModels.insert(TagModel(tagID, modelID))
            }
          })
        }
      }
    }
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}