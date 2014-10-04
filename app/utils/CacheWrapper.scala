package utils

import play.api.cache.Cache
import models.User
import securesocial.core.SecureSocial
import play.api.Play.current
import securesocial.core.IdentityId
import models.Tables

object CacheWrapper {
  
  def user(identityId: IdentityId): User = {
    Cache.get(identityId.userId) match {
      case Some(user) => user.asInstanceOf[User]
      case None => Tables.Users.findByIdentityId(identityId).get
    }
  }

}