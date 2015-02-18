package securesocial.museum

import play.api.{ Logger, Application }
import securesocial.core._
import securesocial.core.providers._
import securesocial.core.services._
import models._
import org.joda.time.DateTime
import scala.concurrent.Future
import play.api._
import play.api.mvc._
import models._
import play.api.db.slick.DBAction
import play.api.db.slick._
import play.api.db.DB._
import models.Tags
import scala.slick.driver.PostgresDriver.simple._
import scala.slick.driver.PostgresDriver.simple.{ Session => SlickSession }
import play.api._
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import scala.slick.driver.PostgresDriver.simple._
import models.notInDB.Role

class MyUserService extends UserService[User] {

  def basicProfileToUser(bp: BasicProfile): User = {
    User(
      providerId = bp.providerId,
      userId = bp.userId,
      firstName = bp.firstName,
      lastName = bp.lastName,
      fullName = bp.fullName,
      email = bp.email,
      avatarUrl = bp.avatarUrl,
      authMethod = bp.authMethod,
      oAuth1Info = bp.oAuth1Info,
      oAuth2Info = bp.oAuth2Info,
      passwordInfo = bp.passwordInfo,
      role = Role.UnInitiated,
      organizationId = 1
      )
  }
  
  def userToBasicProfile(user: User): BasicProfile = {
    BasicProfile(
        providerId = user.providerId,
        userId = user.userId,
        firstName = user.firstName,
        lastName = user.lastName,
        fullName = user.fullName,
        email = user.email,
        avatarUrl = user.avatarUrl,
        authMethod = user.authMethod,
        oAuth1Info = user.oAuth1Info,
        oAuth2Info = user.oAuth2Info,
        passwordInfo = user.passwordInfo)
  }

  /**
   * Finds a SocialUser that maches the specified id
   *
   * @param providerId the provider id
   * @param userId the user id
   * @return an optional profile
   */
  def find(providerId: String, userId: String): Future[Option[BasicProfile]] = Future {
    Tables.Users.find(providerId, userId) match {
      case Some(user) => Some(userToBasicProfile(user))
      case None => None
    }
  }

  /**
   * Finds a profile by email and provider
   *
   * @param email - the user email
   * @param providerId - the provider id
   * @return an optional profile
   */
  def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] = Future {
    Tables.Users.findByEmailAndProvider(email, providerId) match {
      case Some(user) => Some(userToBasicProfile(user))
      case None => None
    }
  }

  /**
   * Saves a profile.  This method gets called when a user logs in, registers or changes his password.
   * This is your chance to save the user information in your backing store.
   *
   * @param profile the user profile
   * @param mode a mode that tells you why the save method was called
   */
  def save(profile: BasicProfile, mode: SaveMode): Future[User] = {
    mode match {
      case SaveMode.LoggedIn => 
        Future(Tables.Users.find(profile.providerId, profile.userId).get)
        case SaveMode.PasswordChange => ???
        case SaveMode.SignUp => Future(Tables.Users.save(basicProfileToUser(profile)))
    }
  }

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
  def passwordInfoFor(user: User): Future[Option[PasswordInfo]] = Future(Tables.Users.findById(user.uid.get).get.passwordInfo)

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
  def saveToken(token: MailToken): Future[MailToken] = Future(Tables.Tokens.save(token))

  /**
   * Finds a token
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   * @param token the token id
   * @return
   */
  def findToken(token: String): Future[Option[MailToken]] = Future(Tables.Tokens.findById(token))

  /**
   * Deletes a token
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   * @param uuid the token id
   */
  def deleteToken(uuid: String): Future[Option[MailToken]] = Future(Tables.Tokens.delete(uuid))

  /**
   * Deletes all expired tokens
   *
   * Note: If you do not plan to use the UsernamePassword provider just provide en empty
   * implementation
   *
   */
  def deleteExpiredTokens() = Tables.Tokens.deleteExpiredTokens(DateTime.now)
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
