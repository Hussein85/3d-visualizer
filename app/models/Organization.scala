package models

import play.api._
import play.api.Play.current
import scala.slick.driver.PostgresDriver.simple._
import java.sql.Date
import org.joda.time.DateTime
import com.github.tototoshi.slick.PostgresJodaSupport._

case class Organization(id: Option[Int], name: String)

class Organizations(tag: slick.driver.PostgresDriver.simple.Tag)extends Table[Organization](tag, "ORGANIZATION") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def name = column[String]("NAME")
 
  def * = (id.?, name) <> (Organization.tupled, Organization.unapply _)
}

object Organizations {

  val organizations = TableQuery[Organizations]
  
  def insert(organization: Organization)(implicit s: Session): Int = {
    (organizations returning organizations.map(_.id)) += organization
  }
  
  def get(id: Int)(implicit s: Session): Option[Organization] = {
    organizations.filter(_.id === id).firstOption
    }
  }