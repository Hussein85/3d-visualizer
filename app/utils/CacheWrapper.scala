package utils

import play.api.cache.Cache
import models.User
import securesocial.core.SecureSocial
import play.api.Play.current
import models.Tables

object CacheWrapper {
  
  def user(uid: Long): User = {
    Cache.get(uid.toString()) match {
      case Some(user) => user.asInstanceOf[User]
      case None => Tables.Users.findById(uid).get
    }
  }

}