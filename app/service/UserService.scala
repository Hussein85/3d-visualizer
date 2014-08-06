package service

import play.api.{Logger, Application}
import securesocial.core._
import securesocial.core.providers.Token
import securesocial.core.IdentityId
import models.Tokens
import models.Tables
import org.joda.time.DateTime


class UserService(application: Application) extends UserServicePlugin(application) {

  def find(id: IdentityId): Option[Identity] = {
    val identity = Tables.Users.findByIdentityId(id)
    Logger.info("Identity: " + identity.getOrElse("No identity found").toString())
    identity
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    Tables.Users.findByEmailAndProvider(email, providerId)
  }

  def save(user: Identity): Identity = {
    Tables.Users.save(user)
  }

  def save(token: Token) {
    Tables.Tokens.save(token)
  }

  def findToken(token: String): Option[Token] = {
    Tables.Tokens.findById(token)
  }

  def deleteToken(uuid: String) {
    Tables.Tokens.delete(uuid)
  }

  def deleteExpiredTokens() {
   Tables.Tokens.deleteExpiredTokens(DateTime.now)
  }
}