package securesocial.museum

import securesocial.core.Authorization
import securesocial.core.Identity
import models.Tables
import models.notInDB.Role
import utils.CacheWrapper
import play.api.Logger

object Normal extends Authorization {
  def isAuthorized(identity: Identity): Boolean = {
    Logger.info("ROLE: " + CacheWrapper.user(identity.identityId).role)
    CacheWrapper.user(identity.identityId).role match {
      case Role.UnInitiated => false
      case Role.Admin | Role.Contributer | Role.Consumer => true
    }
  }
}

object Admin extends Authorization {
  def isAuthorized(identity: Identity): Boolean = {
    Logger.info(CacheWrapper.user(identity.identityId).role)
    CacheWrapper.user(identity.identityId).role match {
      case Role.Admin => true
      case _ => false
    }
  }
}

object Contributer extends Authorization {
  def isAuthorized(identity: Identity): Boolean = {
    CacheWrapper.user(identity.identityId).role match {
      case Role.Admin | Role.Contributer => true
      case _ => false
    }
  }
}