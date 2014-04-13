package utils

import java.io.File;
import play.api._

object Constants {
  def uploadDir: File = {
    
    val uploadDir = new File("upload")
    if(uploadDir.mkdir()){
      Logger.info("Created: " + uploadDir.getAbsolutePath() )
    }
    uploadDir
}
  }
 