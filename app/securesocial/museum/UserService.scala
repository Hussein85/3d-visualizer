package securesocial.museum

import play.api.{Logger, Application}
import securesocial.core._
import securesocial.core.providers._
import securesocial.core.services._
import models._
import org.joda.time.DateTime


class MyUserService extends UserService[User] {

  def find(providerId: String, userId: String): Option[User] = {
    val user = Tables.Users.find(providerId, userId)
    Logger.info("Identity: " + user.getOrElse("No user found").toString())
    user
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[User] = {
    Tables.Users.findByEmailAndProvider(email, providerId)
  }

  def save(user: User, mode: SaveMode) = {
    Tables.Users.save(user)
  }

  def saveToken(token: MailToken) {
    Tables.Tokens.save(token)
  }

  def findToken(token: String): Option[MailToken] = {
    Tables.Tokens.findById(token)
  }

  def deleteToken(uuid: String) {
    Tables.Tokens.delete(uuid)
  }

  def deleteExpiredTokens() {
   Tables.Tokens.deleteExpiredTokens(DateTime.now)
  }
}