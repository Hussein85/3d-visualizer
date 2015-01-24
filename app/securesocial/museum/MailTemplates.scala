package securesocial.museum

import play.api.data.Form
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import play.twirl.api.{ Html, Txt }
import securesocial.core.{ BasicProfile, RuntimeEnvironment }
import securesocial.controllers.MailTemplates

object MyMailTemplates {
  /**
   * The default mails.
   */
  class Default(env: RuntimeEnvironment[_]) extends MailTemplates {
    implicit val implicitEnv = env
    def getSignUpEmail(token: String)(implicit request: RequestHeader, lang: Lang): (Option[Txt], Option[Html]) = {
      (None, Some(securesocial.views.html.mails.signUpEmail(token)))
    }

    def getAlreadyRegisteredEmail(user: BasicProfile)(implicit request: RequestHeader, lang: Lang): (Option[Txt], Option[Html]) = {
      (None, Some(securesocial.views.html.mails.alreadyRegisteredEmail(user)))
    }

    def getWelcomeEmail(user: BasicProfile)(implicit request: RequestHeader, lang: Lang): (Option[Txt], Option[Html]) = {
      (None, Some(securesocial.views.html.mails.welcomeEmail(user)))
    }

    def getUnknownEmailNotice()(implicit request: RequestHeader, lang: Lang): (Option[Txt], Option[Html]) = {
      (None, Some(securesocial.views.html.mails.unknownEmailNotice()))
    }

    def getSendPasswordResetEmail(user: BasicProfile, token: String)(implicit request: RequestHeader, lang: Lang): (Option[Txt], Option[Html]) = {
      (None, Some(securesocial.views.html.mails.passwordResetEmail(user, token)))
    }

    def getPasswordChangedNoticeEmail(user: BasicProfile)(implicit request: RequestHeader, lang: Lang): (Option[Txt], Option[Html]) = {
      (None, Some(securesocial.views.html.mails.passwordChangedNotice(user)))
    }
    /*
     def getSignUpEmail(token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.securesocialCustom.mails.signUpEmail(token)))
  }

  def getAlreadyRegisteredEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(securesocial.views.html.mails.alreadyRegisteredEmail(user)))
  }

  def getWelcomeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.securesocialCustom.mails.welcomeEmail(user)))
  }

  def getUnknownEmailNotice()(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(securesocial.views.html.mails.unknownEmailNotice(request)))
  }

  def getSendPasswordResetEmail(user: Identity, token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(securesocial.views.html.mails.passwordResetEmail(user, token)))
  }

  def getPasswordChangedNoticeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(securesocial.views.html.mails.passwordChangedNotice(user)))
  }*/
  }
}