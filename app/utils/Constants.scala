package utils

import java.io.File
import play.api.libs.json._
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

object EnumUtils {
  def enumReads[E <: Enumeration](enum: E): Reads[E#Value] =
    new Reads[E#Value] {
      def reads(json: JsValue): JsResult[E#Value] = json match {
        case JsString(s) => {
          try {
            JsSuccess(enum.withName(s))
          } catch {
            case _: NoSuchElementException =>
               JsError(s"Enumeration expected of type: '${enum.getClass}', but it does not appear to contain the value: '$s'")
          }
        }
        case _ => JsError("String value expected")
      }
  }

  implicit def enumWrites[E <: Enumeration]: Writes[E#Value] =
    new Writes[E#Value] {
      def writes(v: E#Value): JsValue = JsString(v.toString)
    }

  implicit def enumFormat[E <: Enumeration](enum: E): Format[E#Value] = {
    Format(enumReads(enum), enumWrites)
  }
}


object Role extends Enumeration{
  type Role = Value
  val Admin, Contributer, Consumer, UnInitiated = Value;
  
  implicit val enumReads: Reads[Role] = EnumUtils.enumReads(Role)

  implicit def enumWrites: Writes[Role] = EnumUtils.enumWrites

  
}

abstract class DBEnum extends Enumeration {

  import scala.slick.driver.PostgresDriver.simple._
  import scala.slick.driver.PostgresDriver.MappedJdbcType

  implicit val enumMapperInt = MappedJdbcType.base[Value, Int](_.id, this.apply)
  implicit val enumMapperString = MappedJdbcType.base[Value, String](_.toString(), this.withName(_))
}




