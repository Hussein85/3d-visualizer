package service

import models.User
import securesocial.core.Authorization
import securesocial.core.Identity
import models.Tables


case class Admin() extends Authorization {
  def isAuthorized(identity: Identity): Boolean = {
    Tables.Users.findByIdentityId(identity.identityId).get.admin
  }
}