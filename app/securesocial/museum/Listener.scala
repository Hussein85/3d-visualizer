package securesocial.museum

import securesocial.core.LogoutEvent
import securesocial.core.PasswordChangeEvent
import securesocial.core.PasswordResetEvent
import securesocial.core.LoginEvent
import securesocial.core.SignUpEvent
import play.api.mvc.RequestHeader
import securesocial.core._
import play.api.Play.current
import play.api.Logger
import play.api.mvc.Session
import securesocial.core.EventListener
import com.typesafe.plugin.MailerPlugin
import com.typesafe.plugin.use
import scala.slick.ast.GetOrElse
import models.Models
import models.Users
import play.api.cache.Cache
import models.Tables
import models._

class Listener/*(app: play.api.Application) extends EventListener {

  def onEvent[U](event: Event[U], request: RequestHeader, session: Session): Option[Session] = {
    val eventName: String = event match {
      case LoginEvent(u: User) => {
        Cache.set(u.uid.get.toString, Tables.Users.findById(u.uid.get))
        "login"
      }
      case LogoutEvent(u: User) => {
        Cache.remove(u.uid.toString())
        "logout"
      }
      case SignUpEvent(u: User) => {
        import com.typesafe.plugin._
        val mail = use[MailerPlugin].email
        mail.setSubject("A new user has signed up.")
        mail.setRecipient("palmqvist.thomas@gmail.com")
        //or use a list
        mail.setFrom("Museumarkiv <museumarkiv@gmail.com>")

        //mail.sendHtml("<html>html</html>" )

        mail.send{
          val email = u.email.getOrElse("No email")
          val fullname = u.fullName
          s"User with email: $email have signed up, please log in and assign a role for $fullname"
        }
        //sends both text and html
        //	mail.send( "text", "<html>html</html>")
        "signup"
      }
      case  PasswordResetEvent(u: User) => "password reset"
      case  PasswordChangeEvent(u: User) => "password change"
      case _ => "Other event"
    }
    Logger.info("traced %s event for user %s".format(eventName, event.user.asInstanceOf[User].fullName))
    None
  }
}*/