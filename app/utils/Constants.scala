package utils

import java.io.File
import play.api._


object Constants {
  def uploadDir: File = {

    val uploadDir = new File(Play.current.configuration.getString("uploadPath").get.replace("~", System.getProperty("user.home")))
    if (uploadDir.mkdir()) {
      Logger.info("Created: " + uploadDir.getAbsolutePath())
    }
    uploadDir
  }
}

object Role extends Enumeration {
  type Role = Value
  val Admin, Contributer, Consumer, UnInitiated = Value;
}

abstract class DBEnum extends Enumeration {

  import scala.slick.driver.PostgresDriver.simple._
  import scala.slick.driver.PostgresDriver.MappedJdbcType

  implicit val enumMapperInt = MappedJdbcType.base[Value, Int](_.id, this.apply)
  implicit val enumMapperString = MappedJdbcType.base[Value, String](_.toString(), this.withName(_))
}




