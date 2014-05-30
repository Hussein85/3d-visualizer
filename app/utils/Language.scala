package utils

import play.api.i18n.Lang
import play.api._
import play.api.mvc.Session
import play.api.mvc.Request
import play.api.mvc.RequestHeader
import scala.slick.ast.GetOrElse

object Language {

  private val defaultLanguage = "se"

  def langCode(request: Request[Any]): Lang = {
    val lang: Lang = Lang.get(request.session.get("languageCode").getOrElse(defaultLanguage)).getOrElse(Lang(defaultLanguage))
    lang
  }

}