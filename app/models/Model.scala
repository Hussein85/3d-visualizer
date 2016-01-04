
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

case class Model(id: Option[Int], 
    name: String, 
    userID: String,
    date: DateTime,    
    material: String, 
    location: String, 
    latitude: Double, 
    longitude: Double, 
    text: String, 
    timestamp: DateTime, 
    published: Boolean, 
    organizationId: Int)

object Model {

  implicit val dtwrites: Writes[DateTime] = Writes { (dt: DateTime) => JsString(dt.year.get.toString) }

  implicit val modelWrites: Writes[Model] = (
    (JsPath \ "id").write[Option[Int]] and
    (JsPath \ "name").write[String] and
    (JsPath \ "userId").write[String] and
    Writes.at[DateTime]((JsPath \ "date"))(dtwrites) and
    (JsPath \ "material").write[String] and
    (JsPath \ "location").write[String] and
    (JsPath \ "latitude").write[Double] and
    (JsPath \ "longitude").write[Double] and
    (JsPath \ "text").write[String] and
    (JsPath \ "timeStamp").writeNullable[DateTime].contramap((_: DateTime) => None) and
    (JsPath \ "published").write[Boolean] and
    (JsPath \ "organizationId").write[Int] )(unlift(models.Model.unapply))

}

class Models(tag: slick.driver.PostgresDriver.simple.Tag) extends Table[Model](tag, "MODEL") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def name = column[String]("NAME")
  def userID = column[String]("USER_ID")
  def date = column[DateTime]("DATE")
  def material = column[String]("MATERIAL")
  def location = column[String]("LOCATION")
  def latitude = column[Double]("LATITUDE")
  def longitude = column[Double]("LONGITUDE")
  def text = column[String]("TEXT", O.DBType("TEXT"))
  def timestamp = column[DateTime]("TIMESTAMP")
  def published = column[Boolean]("PUBLISHED")
  def organizationId = column[Int]("ORGANIZATION_ID")
  def * = (id.?, name, userID, date, material, location, latitude, longitude, text, timestamp, published, organizationId) <>
    ((Model.apply _).tupled, Model.unapply _)
}

object Models {

  val models = TableQuery[Models]

  def insert(model: Model)(implicit s: Session): Int = {
    (models returning models.map(_.id)) += model
  }

  def all(organizationId: Int)(implicit s: Session): List[Model] = {
    models.filter(_.organizationId === organizationId).list
  }

  def published(organizationId: Int)(implicit s: Session): List[Model] = {
    models.filter(m => (
        m.published === true &&
        m.organizationId === organizationId)).list
  }

  def setPublished(id: Int, published: Boolean)(implicit s: Session) = {
    models.filter(_.id === id)
      .map(m => m.published)
      .update(true)
  }

  def unpublished(implicit s: Session): List[Model] = {
    models.filter(_.published === false).list
  }

  def get(id: Int, organizationId: Int)(implicit s: Session): Option[Model] = {
    models.filter(m => (
      m.id === id &&
      m.published === true &&
      m.organizationId === organizationId)).firstOption
  }

}