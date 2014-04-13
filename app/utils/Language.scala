package utils

import play.api.i18n.Lang
import play.api.Play


trait Language {
  
  private val defaultLanguage = "se"
  private var language = defaultLanguage

  implicit val lang = Lang.get(language).getOrElse("defaultLanguage")
  
  def setLanguage(langCode: String) = {
    language = langCode
  }
  
  def getLanguage(langCode: String) = {
    language
  }
  
}