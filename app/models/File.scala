package models

import play.api._
import play.api.Play.current
import scala.slick.driver.PostgresDriver.simple._
import java.sql.Date
import org.joda.time.DateTime
import com.github.tototoshi.slick.PostgresJodaSupport._

case class File(id: String, modelId: Int, `type`: String, timestamp: DateTime, finished: Boolean, userId: Long)

class Files(tag: slick.driver.PostgresDriver.simple.Tag) extends Table[File](tag, "FILE") {
  def id = column[String]("ID", O.PrimaryKey)
  def modelId = column[Int]("MODEL_ID")
  def `type` = column[String]("TYPE")
  def timestamp = column[DateTime]("TIMESTAMP")
  def finished = column[Boolean]("FINISHED")
  def userId = column[Long]("USER_ID")

  def * = (id, modelId, `type`, timestamp, finished, userId) <> (File.tupled, File.unapply _)
}

object Files {

  val Files = TableQuery[Files]

  def insert(File: File)(implicit s: Session) = {
    Files += File
  }

  def get(id: String)(implicit s: Session): Option[File] = {
    Files.filter(_.id === id).firstOption
  }
}