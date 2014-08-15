package service

import models.User
import securesocial.core.Authorization
import securesocial.core.Identity
import models.Tables
import utils.Role._
import utils.Role

object Normal extends Authorization {
  def isAuthorized(identity: Identity): Boolean = {
    Tables.Users.findByIdentityId(identity.identityId).get.role match {
      case UnInitiated => false
      case Role.Admin | Role.Contributer | Consumer => true
    }
  }
}

object Admin extends Authorization {
  def isAuthorized(identity: Identity): Boolean = {
    Tables.Users.findByIdentityId(identity.identityId).get.role match {
      case Role.Admin  => true
      case _ => false
    }
  }
}

object Contributer extends Authorization {
  def isAuthorized(identity: Identity): Boolean = {
    Tables.Users.findByIdentityId(identity.identityId).get.role match {
      case Role.Admin | Role.Contributer  => true
      case _ => false
    }
  }
}