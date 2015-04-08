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
    log.info("GW_HOME is set to: ${ System.properties.GW_HOME }")
    fileSystemService.init()

    new Role(authority: 'ROLE_USER').save()
    def adminRole = new Role(authority: 'ROLE_ADMIN').save()
    def admin = new User(username: 'admin', password: 'admin', enabled: true, email: 'grid.wars@cern.ch')
    admin.save()
    new UserRole(user: admin, role: adminRole).save()
    def admin2 = new User(username: 'admin2', password: 'admin', enabled: true, email: 'grid.wars2@cern.ch')
    admin2.save()
    new UserRole(user: admin2, role: adminRole).save()
    def admin3 = new User(username: 'admin3', password: 'admin', enabled: true, email: 'grid.wars2@cern.ch')
    admin3.save()
    new UserRole(user: admin3, role: adminRole).save()
    def admin4 = new User(username: 'admin3', password: 'admin', enabled: true, email: 'grid.wars2@cern.ch')
    admin4.save()
    new UserRole(user: admin4, role: adminRole).save()


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
        body "Time: ${ new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss").format(new Date()) }"
      }
    }
  }
}
