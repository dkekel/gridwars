import cern.ais.gridwars.User
import grails.util.Environment
import org.apache.commons.codec.digest.DigestUtils

class BootStrap
{
  def agentUploadService
  def matchmakingService
  def gameExecutionService
  def fileSystemService

  def init = { servletContext ->
    fileSystemService.init()

    if (Environment.current == Environment.DEVELOPMENT) {
      ["bot.Main", "ru.tversu.gridwars.TverSUbot81"].eachWithIndex { jarName, it ->
        def u = new User()
        u.email = "email$it@cern.ch"
        u.username = "user$it"
        u.salt = "salt";
        def pass = "user$it"
        u.hashedPassword = DigestUtils.sha256Hex(u.salt + pass)
        u.save(failOnError: true, validate: true)
        log.debug("$u.username $pass")
        jarName
        //agentUploadService.processJarUpload(new File('/Users/seagull/Dev/cern/gridwars/res', jarName + '.jar'), jarName, u)
      }
    }

    def match = matchmakingService.nextMatch
    if (match)
      gameExecutionService.executeGame(match)
  }

  def destroy = {
  }
}
