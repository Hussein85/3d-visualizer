package utils

import java.io.File;
import play.api._

object Constants {
  def uploadDir: File = {

    val uploadDir = new File(Play.current.configuration.getString("uploadPath").get.replace("~",System.getProperty("user.home")))
    if (uploadDir.mkdir()) {
      Logger.info("Created: " + uploadDir.getAbsolutePath())
    }
    uploadDir
  }
}
 