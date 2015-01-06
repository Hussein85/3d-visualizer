package utils

import play.api.mvc._
import play.api.libs.Files._
import play.api._
import play.api.Play.current
import fly.play.s3._
import fly.play.aws.auth.SimpleAwsCredentials
import fly.play.aws.PlayConfiguration
import scala.concurrent.ExecutionContext.Implicits.global
import java.nio.file.Files

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

  def saveFormFileToS3(request: Request[MultipartFormData[TemporaryFile]], requestName: String): String = {
    request.body.file(requestName).map { objectFile =>
      import java.io.File
      def uuid = java.util.UUID.randomUUID.toString
      val filename = objectFile.filename
      val uniqeFilename = s"$uuid-$filename".replace(" ", "-")
      val contentType = objectFile.contentType
      implicit val awsCredentials = SimpleAwsCredentials(PlayConfiguration("aws.accessKeyId"), PlayConfiguration("aws.secretKey"))
      val bucket = S3("museum-dev")
      val file = Files.readAllBytes(objectFile.ref.file.toPath())
      val fileSize = file.length
      val result = bucket + BucketFile(uniqeFilename, contentType.getOrElse("missing"), file, Some(PRIVATE))
      result.map { unit =>
        Logger.info(s"Saved file with name: $uniqeFilename")
      }
        .recover {
          case S3Exception(status, code, message, originalXml) => Logger.info("Error: " + message)
        }
      uniqeFilename
    }.getOrElse {
      Logger.info(s"Missing object file: $requestName")
      ""
    }
  }

}