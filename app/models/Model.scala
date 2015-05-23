package models

import play.api._
import play.api.libs.json._
import play.api.libs.json.Writes._
import play.api.libs.functional.syntax._
import play.api.Play.current
import scala.slick.driver.PostgresDriver.simple._
import java.sql.Date
import org.joda.time.DateTime
import com.github.tototoshi.slick.PostgresJodaSupport._

case class Model(id: Option[Int], name: String, userID: String, date: DateTime, material: String, location: String, text: String, pathObject: Option[String], pathTexure: Option[String], pathThumbnail: Option[String])

object Model {

  implicit val dtwrites: Writes[DateTime] = Writes { (dt: DateTime) => JsString(dt.year.get.toString) }
  
  implicit val modelWrites: Writes[Model] = (
    (JsPath \ "id").write[Option[Int]] and
    (JsPath \ "name").write[String] and
    (JsPath \ "userId").write[String] and
    Writes.at[DateTime]((JsPath \ "date"))(dtwrites) and
    (JsPath \ "material").write[String] and
    (JsPath \ "location").write[String] and
    (JsPath \ "text").write[String] and
    (JsPath \ "f1").write[Option[String]] and
    (JsPath \ "f2").write[Option[String]] and
    (JsPath \ "f3").write[Option[String]])(unlift(models.Model.unapply))
    
}

class Models(tag: slick.driver.PostgresDriver.simple.Tag) extends Table[Model](tag, "MODEL") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def name = column[String]("NAME")
  def userID = column[String]("USER_ID")
  def date = column[DateTime]("DATE")
  def material = column[String]("MATERIAL")
  def location = column[String]("LOCATION")
  def text = column[String]("TEXT", O.DBType("TEXT"))
  def pathObject = column[Option[String]]("PATH_OBJECT")
  def pathTexure = column[Option[String]]("PATH_TEXTURE")
  def pathThumbnail = column[Option[String]]("PATH_THUMBNAIL")
  def * = (id.?, name, userID, date, material, location, text, pathObject, pathTexure, pathThumbnail) <>
    ((Model.apply _).tupled, Model.unapply _)
}

object Models {

  val models = TableQuery[Models]

  def insert(model: Model)(implicit s: Session): Int = {
    (models returning models.map(_.id)) += model
  }

  def all(implicit s: Session): List[Model] = {
    models.list
  }

  def get(id: Int)(implicit s: Session): Option[Model] = {
    models.filter(_.id === id).firstOption
  }

  def get(pathObject: String)(implicit s: Session): Option[Model] = {
    models.filter(_.pathObject === pathObject).firstOption
  }

}