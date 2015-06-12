package models

import play.api._
import play.api.Play.current
import scala.slick.driver.PostgresDriver.simple._
import java.sql.Date
import org.joda.time.DateTime
import com.github.tototoshi.slick.PostgresJodaSupport._

case class File(id: String, modelId: Int, `type`: String, timestamp: DateTime, finished: Boolean, userId: String)

class Files(tag: slick.driver.PostgresDriver.simple.Tag) extends Table[File](tag, "FILE") {
  def id = column[String]("ID", O.PrimaryKey)
  def modelId = column[Int]("MODEL_ID")
  def `type` = column[String]("TYPE")
  def timestamp = column[DateTime]("TIMESTAMP")
  def finished = column[Boolean]("FINISHED")
  def userId = column[String]("USER_ID")

  def * = (id, modelId, `type`, timestamp, finished, userId) <> (File.tupled, File.unapply _)
}

object Files {

  val Files = TableQuery[Files]

  def insert(File: File)(implicit s: Session) = {
    Files += File
  }

  def setFinished(id: String)(implicit s: Session) = {
    Files
      .filter(_.id === id)
      .map(f => f.finished)
      .update(true)
  }

  def get(id: String)(implicit s: Session): File = {
    Files.filter(_.id === id).first
  }

  def modelFiles(modelId: Int)(implicit s: Session): List[File] = {
    Files.filter(_.modelId === modelId).list
  }

}