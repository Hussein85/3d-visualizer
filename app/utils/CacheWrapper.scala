package utils

import play.api.cache.Cache
import models.User
import securesocial.core.SecureSocial
import play.api.Play.current
import models.Tables

object CacheWrapper {
  
  def user(providerId: String, userId: String): User = {
    Cache.get(userId) match {
      case Some(user) => user.asInstanceOf[User]
      case None => Tables.Users.find(providerId, userId).get
    }
  }

}