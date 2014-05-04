package models

import play.api._
import play.api.Play.current
import scala.slick.driver.PostgresDriver.simple._
import java.sql.Date
import org.joda.time.DateTime
import com.github.tototoshi.slick.PostgresJodaSupport._

case class Model(id: Option[Int], name: String, userID: Int, date: DateTime, material: String, location: String, text: String, pathObject: String, pathTexure: String)

class Models(tag: slick.driver.PostgresDriver.simple.Tag) extends Table[Model](tag, "MODEL") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def name = column[String]("NAME")
  def userID = column[Int]("USER_ID")
  def date = column[DateTime]("DATE")
  def material = column[String]("MATERIAL")
  def location = column[String]("LOCATION")
  def text = column[String]("TEXT")
  def pathObject = column[String]("PATH_OBJECT")
  def pathTexure = column[String]("PATH_TEXTURE")
  def * = (id.?, name, userID, date, material, location, text, pathObject, pathTexure) <> (Model.tupled, Model.unapply _)
}

object Models {

  val models = TableQuery[Models]

  def insert(model: Model)(implicit s: Session) {
    models.insert(model)
  }

  def all(implicit s: Session): List[Model] = {
    models.list
  }

}