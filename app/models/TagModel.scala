import play.api._
import play.api.Play.current
import scala.slick.driver.PostgresDriver.simple._

case class TagModel(tagID: Int, modelID: Int)

class TagModels(tag: slick.driver.PostgresDriver.simple.Tag) extends Table[TagModel](tag, "TAG_MODEL") {
  def tagID = column[Int]("TAG_ID")
  def modelID = column[Int]("MODEL_ID")
  def * = (tagID, modelID) <> (TagModel.tupled, TagModel.unapply _)
  def uniqueName = index("IDX_MODEL_TAG", (tagID, modelID), unique = true)
}

object TagModels {

  val tagModels = TableQuery[TagModels]

  def insert(tagModel: TagModel)(implicit s: Session) {
    tagModels.insert(tagModel)
  }
  
  def all(implicit s: Session): List[TagModel] = {
     tagModels.list
  }

}