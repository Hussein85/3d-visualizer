package securesocial.museum

import securesocial.core.LogoutEvent
import securesocial.core.PasswordChangeEvent
import securesocial.core.PasswordResetEvent
import securesocial.core.LoginEvent
import securesocial.core.SignUpEvent
import play.api.mvc.RequestHeader
import securesocial.core.Event
import play.api.Play.current
import play.api.Logger
import play.api.mvc.Session
import securesocial.core.EventListener
import com.typesafe.plugin.MailerPlugin
import com.typesafe.plugin.use
import scala.slick.ast.GetOrElse

class Listener(app: play.api.Application) extends EventListener {
  override def id: String = "my_event_listener"

  def onEvent(event: Event, request: RequestHeader, session: Session): Option[Session] = {
    val eventName = event match {
      case e: LoginEvent => "login"
      case e: LogoutEvent => "logout"
      case e: SignUpEvent => {
        import com.typesafe.plugin._
        val mail = use[MailerPlugin].email
        mail.setSubject("A new user has signed up.")
        mail.setRecipient("palmqvist.thomas@gmail.com")
        //or use a list
        mail.setFrom("Museumarkiv <museumarkiv@gmail.com>")

        //mail.sendHtml("<html>html</html>" )

        mail.send{
          val email = e.user.email.getOrElse("No email")
          val fullname = e.user.fullName
          s"User with email: $email have signed up, please log in and assign a role for $fullname"
        }
        //sends both text and html
        //	mail.send( "text", "<html>html</html>")
        "signup"
      }
      case e: PasswordResetEvent => "password reset"
      case e: PasswordChangeEvent => "password change"
    }
    Logger.info("traced %s event for user %s".format(eventName, event.user.fullName))
    None
  }
}