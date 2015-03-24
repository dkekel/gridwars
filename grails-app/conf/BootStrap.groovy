import cern.ais.gridwars.security.Role
import cern.ais.gridwars.security.User
import cern.ais.gridwars.security.UserRole
import grails.util.Environment

import java.text.SimpleDateFormat

class BootStrap
{
  def agentUploadService
  def matchmakingService
  def gameExecutionService
  def fileSystemService
  def mailService
  def configService

  def init = { servletContext ->
    fileSystemService.init()


    new Role(authority: 'ROLE_USER').save()
    def adminRole = new Role(authority: 'ROLE_ADMIN').save()
    def admin = new User(username: 'admin', password: 'admin', enabled: true, email: 'admin@servername')
    admin.save()
    new UserRole(user: admin, role: adminRole).save()


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
      reportServerStatus("Grid Wars server is active!")
  }

  def destroy = {
    reportServerStatus("Grid Wars server is dying!")
  }

  private reportServerStatus(String subjectText) {
    if (configService.sendMailsToAdmin) {
      mailService.sendMail {
        to configService.admins
        subject subjectText
        body "Time: ${ new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss").format(new Date()) }"
      }
    }
  }
}
