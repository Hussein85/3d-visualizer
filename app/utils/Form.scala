package utils

import play.api.mvc._
import play.api.libs.Files._
import play.api._

object FormHelper {
  
  def saveFormFile(request: Request[MultipartFormData[TemporaryFile]], requestName: String): String = {
    request.body.file(requestName).map { objectFile =>
      import java.io.File
      def uuid = java.util.UUID.randomUUID.toString
      val filename = objectFile.filename
      val uniqeFilename = s"$uuid-$filename".replace(" ", "-")
      val contentType = objectFile.contentType
      val uploadPath = Constants.uploadDir.getPath + "/" + uniqeFilename
      objectFile.ref.moveTo(new File(uploadPath), true)
      Logger.info(s"Saved file to: $uploadPath")
      uniqeFilename
    }.getOrElse {
      Logger.info(s"Missing object file: $requestName")
      ""
    }
  }

}