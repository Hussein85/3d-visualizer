package models

import play.api._
import play.api.Play.current
import scala.slick.driver.PostgresDriver.simple._

case class Tag(id: Option[Int], name: String)

class Tags(tag: slick.driver.PostgresDriver.simple.Tag) extends Table[Tag](tag, "TAG") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def name = column[String]("NAME")
  def * = (id.?, name) <> (Tag.tupled, Tag.unapply _)
  def uniqueName = index("IDX_NAME", name, unique = true)
}

object Tags {

  val tags = TableQuery[Tags]

  def insert(tag: Tag)(implicit s: Session) {
    Logger.info(s"Tried to save tag: $tag")
    tags.insert(tag)
    Logger.info(s"Saved tag: $tag")
  }

}