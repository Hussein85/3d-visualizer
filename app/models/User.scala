package models

import _root_.java.sql.Date
import securesocial.core._
import securesocial.core.providers._

import scala.slick.lifted.ProvenShape
import play.api.{ Logger, Application }

import scala.slick.driver.PostgresDriver.simple._
import com.github.tototoshi.slick.PostgresJodaSupport._


import org.joda.time.DateTime

import models.notInDB.Role

case class User(uid: Option[Long] = None,
  providerId: String,
  userId: String,
  firstName: Option[String],
  lastName: Option[String],
  fullName: Option[String],
  email: Option[String],
  avatarUrl: Option[String],
  authMethod: AuthenticationMethod,
  oAuth1Info: Option[OAuth1Info],
  oAuth2Info: Option[OAuth2Info],
  passwordInfo: Option[PasswordInfo],
  role: String,
  organizationId: Int) extends GenericProfile

class Users(tag: slick.driver.PostgresDriver.simple.Tag) extends Table[User](tag, "user") {

  implicit def string2AuthenticationMethod = MappedColumnType.base[AuthenticationMethod, String](
    authenticationMethod => authenticationMethod.method,
    string => AuthenticationMethod(string))

  implicit def tuple2OAuth1Info(tuple: (Option[String], Option[String])): Option[OAuth1Info] = tuple match {
    case (Some(token), Some(secret)) => Some(OAuth1Info(token, secret))
    case _ => None
  }

  implicit def tuple2OAuth2Info(tuple: (Option[String], Option[String], Option[Int], Option[String])): Option[OAuth2Info] = tuple match {
    case (Some(token), tokenType, expiresIn, refreshToken) => Some(OAuth2Info(token, tokenType, expiresIn, refreshToken))
    case _ => None
  }


  implicit def tuple2PasswordInfo(tuple: (Option[String], Option[String], Option[String])): Option[PasswordInfo] = tuple match {
    case (Some(hasher), Some(password), Some(salt)) => Some(PasswordInfo(hasher, password, Some(salt)))
    case (Some(hasher), Some(password), _) => Some(PasswordInfo(hasher, password, None))
    case _ => None
  }

  def uid = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def userId = column[String]("userId")
  def providerId = column[String]("providerId")
  def email = column[Option[String]]("email")
  def firstName = column[Option[String]]("firstName")
  def lastName = column[Option[String]]("lastName")
  def fullName = column[Option[String]]("fullName")
  def authMethod = column[AuthenticationMethod]("authMethod")
  def avatarUrl = column[Option[String]]("avatarUrl")

  // oAuth 1
  def token = column[Option[String]]("token")
  def secret = column[Option[String]]("secret")

  // oAuth 2
  def accessToken = column[Option[String]]("accessToken")
  def tokenType = column[Option[String]]("tokenType")
  def expiresIn = column[Option[Int]]("expiresIn")
  def refreshToken = column[Option[String]]("refreshToken")

  // passwordInfo 
  def hasher = column[Option[String]]("hasher")
  def password = column[Option[String]]("password")
  def salt = column[Option[String]]("salt")

  // Extended to standard
  def role = column[String]("role")
  def organizationId = column[Int]("ADDRESS_ID")
  lazy val organizations = TableQuery[Organizations]
  def organization = foreignKey("ORAGNIZATION", organizationId, organizations)(_.id)
  
  def email_index = index("idx_a", email, unique = true)

  def * : ProvenShape[User] = {
    val shapedValue = (uid.?,
      userId,
      providerId,
      firstName,
      lastName,
      fullName,
      email,
      avatarUrl,
      authMethod,
      token,
      secret,
      accessToken,
      tokenType,
      expiresIn,
      refreshToken,
      hasher,
      password,
      salt,
      role,
      organizationId).shaped

    shapedValue.<>({
      tuple =>
        User.apply(uid = tuple._1,
          userId = tuple._2,
          providerId = tuple._3,
          firstName = tuple._4,
          lastName = tuple._5,
          fullName = tuple._6,
          email = tuple._7,
          avatarUrl = tuple._8,
          authMethod = tuple._9,
          oAuth1Info = tuple2OAuth1Info(tuple._10, tuple._11),
          oAuth2Info = tuple2OAuth2Info(tuple._12, tuple._13, tuple._14, tuple._15),
          passwordInfo = tuple2PasswordInfo(tuple._16, tuple._17, tuple._18),
          role = tuple._19,
          organizationId = tuple._20)
    }, {
      (u: User) =>
        Some {
          (
            u.uid,
            u.userId,
            u.providerId,
            u.firstName,
            u.lastName,
            u.fullName,
            u.email,
            u.avatarUrl,
            u.authMethod,
            u.oAuth1Info.map(_.token),
            u.oAuth1Info.map(_.secret),
            u.oAuth2Info.map(_.accessToken),
            u.oAuth2Info.flatMap(_.tokenType),
            u.oAuth2Info.flatMap(_.expiresIn),
            u.oAuth2Info.flatMap(_.refreshToken),
            u.passwordInfo.map(_.hasher),
            u.passwordInfo.map(_.password),
            u.passwordInfo.flatMap(_.salt),
            u.role.toString(),
            u.organizationId)
        }
    })
  }

}

class Tokens(tag: slick.driver.PostgresDriver.simple.Tag) extends Table[MailToken](tag, "token") {

  def uuid = column[String]("uuid")

  def email = column[String]("email")

  def creationTime = column[DateTime]("creationTime")

  def expirationTime = column[DateTime]("expirationTime")

  def isSignUp = column[Boolean]("isSignUp")

  def * : ProvenShape[MailToken] = {
    val shapedValue = (uuid, email, creationTime, expirationTime, isSignUp).shaped

    shapedValue.<>({
      tuple =>
        MailToken(uuid = tuple._1,
          email = tuple._2,
          creationTime = tuple._3,
          expirationTime = tuple._4,
          isSignUp = tuple._5)
    }, {
      (t: MailToken) =>
        Some {
          (t.uuid,
            t.email,
            t.creationTime,
            t.expirationTime,
            t.isSignUp)
        }
    })
  }
}

trait WithDefaultSession {

  def withSession[T](block: (Session => T)) = {
    val databaseURL = play.api.Play.current.configuration.getString("db.default.url").get
    val databaseDriver = play.api.Play.current.configuration.getString("db.default.driver").get
    val databaseUser = play.api.Play.current.configuration.getString("db.default.user").getOrElse("")
    val databasePassword = play.api.Play.current.configuration.getString("db.default.password").getOrElse("")

    val database = Database.forURL(url = databaseURL,
      driver = databaseDriver,
      user = databaseUser,
      password = databasePassword)

    database withSession {
      session =>
        block(session)
    }
  }

}

object Tables extends WithDefaultSession {

  val Tokens = new TableQuery[Tokens](new Tokens(_)) {

    def findById(tokenId: String): Option[MailToken] = withSession {
      implicit session =>
        val q = for {
          token <- this
          if token.uuid === tokenId
        } yield token

        q.firstOption
    }

    def save(token: MailToken): MailToken = withSession {
      implicit session =>
        findById(token.uuid) match {
          case None => {
            this.insert(token)
            token
          }
          case Some(existingToken) => {
            val tokenRow = for {
              t <- this
              if t.uuid === existingToken.uuid
            } yield t

            val updatedToken = token.copy(uuid = existingToken.uuid)
            tokenRow.update(updatedToken)
            updatedToken
          }
        }
    }

    def delete(uuid: String) = withSession {
      implicit session =>
        val q = for {
          t <- this
          if t.uuid === uuid
        } yield t

        q.delete
    }

    def deleteExpiredTokens(currentDate: DateTime) = withSession {
      implicit session =>
        val q = for {
          t <- this
          if t.expirationTime < currentDate
        } yield t

        q.delete
    }

  }

  val Users = new TableQuery[Users](new Users(_)) {
    def autoInc = this returning this.map(_.uid)

    def findById(id: Long) = withSession {
      implicit session =>
        val q = for {
          user <- this
          if user.uid === id
        } yield user

        q.firstOption
    }

    def findByEmailAndProvider(email: String, providerId: String): Option[User] = withSession {
      implicit session =>
        val q = for {
          user <- this
          if (user.email === email) && (user.providerId === providerId)
        } yield user

        q.firstOption
    }

    def find(providerId: String, userId: String): Option[User] = withSession {
      implicit session =>
        val q = for {
          user <- this
          if (user.userId === userId) && (user.providerId === providerId)
        } yield user

        val user = q.firstOption
        Logger.info("user: " + user.toString)
        user
    }

    def all = withSession {
      implicit session =>
        val q = for {
          user <- this
        } yield user

        q.list
    }

    def save(user: User): User = withSession {
      implicit session =>
        find(user.providerId, user.userId) match {
          case None => {
            val uid = this.autoInc.insert(user)
            user.copy(uid = Some(uid))
          }
          case Some(existingUser) => {
            val userRow = for {
              u <- this
              if u.uid === existingUser.uid
            } yield u

            val updatedUser = user.copy(uid = existingUser.uid)
            userRow.update(updatedUser)
            updatedUser
          }
        }
    }

  }

}