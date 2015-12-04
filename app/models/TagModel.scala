package models

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
  val models = TableQuery[Models]

  def insert(tagModel: TagModel, organizationId: Int)(implicit s: Session) {
    val model = (for {
         model <- models if model.organizationId === organizationId &&
         model.id === tagModel.modelID
    } yield(model))
    if(model.firstOption.isEmpty){
      Logger.warn("Trying to insert a tag for a model with non matching oragnizationId")
      throw new IllegalAccessException;
    }
    
    tagModels.insert(tagModel)
  }
  
  def all(implicit s: Session): List[TagModel] = {
     tagModels.list
  }

}