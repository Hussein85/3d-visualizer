package models

import play.api._
import play.api.Play.current
import scala.slick.driver.PostgresDriver.simple._
import org.postgresql.util.PSQLException

case class Tag(id: Option[Int], name: String)

class Tags(tag: slick.driver.PostgresDriver.simple.Tag) extends Table[Tag](tag, "TAG") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def name = column[String]("NAME")
  def * = (id.?, name) <> (Tag.tupled, Tag.unapply _)
  def uniqueName = index("IDX_NAME", name, unique = true)
}

object Tags {

  val tags = TableQuery[Tags]
  val tagModels = TableQuery[TagModels]

  def insert(tag: Tag)(implicit s: Session): Int = {
    try {
      (tags returning tags.map(_.id)) += tag
    } catch {
      case e: PSQLException => {
        Logger.info("Tag found in db.")
        tags.filter(_.name === tag.name).first.id.get
      }
    }
  }
  
  def tags(model: Model)(implicit s: Session): List[Tag] = {
		val implicitInnerJoin = for {
		  tm <- tagModels if tm.modelID === model.id
		  tag <- tags if tag.id === tm.tagID
		} yield (tag)		
		implicitInnerJoin.list
  }

  def all(implicit s: Session): List[Tag] = {
    tags.list
  }

}