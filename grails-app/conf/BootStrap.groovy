import cern.ais.gridwars.User
import cern.ais.gridwars.security.Role
import cern.ais.gridwars.security.User
import cern.ais.gridwars.security.UserRole
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

    def role = new Role(authority: 'ROLE_ADMIN').save()
    def admin = new User(username: 'admin', password: 'admin', enabled: true, email: 'admin@servername')
    admin.save()
    new UserRole(user: admin, role: role).save()

    if (Environment.current == Environment.DEVELOPMENT) {
//      ["bot.Main", "ru.tversu.gridwars.TverSUbot81"].eachWithIndex { jarName, it ->
//        def u = new User()
//        u.email = "email$it@cern.ch"
//        u.username = "user$it"
//        u.salt = "salt";
//        def pass = "user$it"
//        u.hashedPassword = DigestUtils.sha256Hex(u.salt + pass)
//        u.save(failOnError: true, validate: true)
//        log.debug("$u.username $pass")
//        jarName
//        //agentUploadService.processJarUpload(new File('/Users/seagull/Dev/cern/gridwars/res', jarName + '.jar'), jarName, u)
//      }
    }

//    def match = matchmakingService.nextMatch
//    if (match)
//      gameExecutionService.executeGame(match)
  }

  def destroy = {
  }
}
