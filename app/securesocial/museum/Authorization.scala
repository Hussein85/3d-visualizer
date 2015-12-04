package securesocial.museum

import securesocial.core.Authorization
import models.Tables
import models.notInDB.Role
import utils.CacheWrapper
import play.api.Logger
import models.User
import play.api.mvc.RequestHeader

object Normal extends Authorization[User] {
  def isAuthorized(user: User, request: RequestHeader): Boolean = {
    CacheWrapper.user(user.uid.get).role match {
      case Role.UnInitiated                              => false
      case Role.Admin | Role.Contributer | Role.Consumer => true
    }
  }
}

object Admin extends Authorization[User] {
  def isAuthorized(user: User, request: RequestHeader): Boolean = {
    CacheWrapper.user(user.uid.get).role match {
      case Role.Admin => true
      case _          => false
    }
  }
}

object Contributer extends Authorization[User] {
  def isAuthorized(user: User, request: RequestHeader): Boolean = {
    CacheWrapper.user(user.uid.get).role match {
      case Role.Admin | Role.Contributer => true
      case _                             => false
    }
  }
}