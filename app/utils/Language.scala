package utils

import play.api.i18n.Lang
import play.api.Play


trait Language {
  
  private val defaultLanguage = "se"

  implicit val lang = Lang.get(defaultLanguage).getOrElse("defaultLanguage")
  
}