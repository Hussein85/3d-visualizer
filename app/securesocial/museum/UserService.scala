package securesocial.museum

import play.api.{Logger, Application}
import securesocial.core._
import securesocial.core.providers._
import securesocial.core.services._
import models._
import org.joda.time.DateTime
import scala.concurrent.Future


class MyUserService extends UserService[User] {
  
  /**
   * Finds a SocialUser that maches the specified id
   *
   * @param providerId the provider id
   * @param userId the user id
   * @return an optional profile
   */
  def find(providerId: String, userId: String): Future[Option[BasicProfile]] = ???

  /**
   * Finds a profile by email and provider
   *
   * @param email - the user email
   * @param providerId - the provider id
   * @return an optional profile
   */
  def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] = ???

  /**
   * Saves a profile.  This method gets called when a user logs in, registers or changes his password.
   * This is your chance to save the user information in your backing store.
   *
   * @param profile the user profile
   * @param mode a mode that tells you why the save method was called
   */
  def save(profile: BasicProfile, mode: SaveMode): Future[User] = ???

  /**
   * Links the current user to another profile
   *
   * @param current The current user instance
   * @param to the profile that needs to be linked to
   */
  def link(current: User, to: BasicProfile): Future[User] = ???

  /**
   * Returns an optional PasswordInfo instance for a given user
   *
   * @param user a user instance
   * @return returns an optional PasswordInfo
   */
  def passwordInfoFor(user: User): Future[Option[PasswordInfo]] = ???

  /**
   * Updates the PasswordInfo for a given user
   *
   * @param user a user instance
   * @param info the password info
   * @return
   */
  def updatePasswordInfo(user: User, info: PasswordInfo): Future[Option[BasicProfile]] = ???

  /**
   * Saves a mail token.  This is needed for users that
   * are creating an account in the system or trying to reset a password
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   * @param token The token to save
   */
  def saveToken(token: MailToken): Future[MailToken] = ???

  /**
   * Finds a token
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   * @param token the token id
   * @return
   */
  def findToken(token: String): Future[Option[MailToken]] = ???

  /**
   * Deletes a token
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   * @param uuid the token id
   */
  def deleteToken(uuid: String): Future[Option[MailToken]] = ???

  /**
   * Deletes all expired tokens
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   */
  def deleteExpiredTokens() = ???
}

  /*def find(providerId: String, userId: String): Future[Option[User]] = {
    val user = Tables.Users.find(providerId, userId)
    Logger.info("Identity: " + user.getOrElse("No user found").toString())
    Future.successful(user)
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
  }*/
