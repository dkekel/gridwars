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
  def quartzScheduler
  def fileSystemService
  def mailService
  def configService
  def transactionManager

  def init = { servletContext ->
    log.info("GW_HOME is set to: ${ System.properties.GW_HOME }")
    fileSystemService.init()
    if (configService.jobsEnabled)
      quartzScheduler.resumeAll()
    else
      quartzScheduler.pauseAll()

    new Role(authority: 'ROLE_USER').save()
    def adminRoles = [new Role(authority: 'ROLE_ADMIN').save(), new Role(authority: 'ADMIN_BOT').save()]

    5.times {
      createAdminAccount(it, adminRoles)
    }

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

	void createAdminAccount(int id, List<Role> adminRoles) {
		def userName = "admin" + (id ?: "")
		if (User.findByUsername(userName))
			return

		def admin = new User(username: userName, password: 'GridWarsCern123', enabled: true, email: 'grid.wars@cern.ch')
		admin.save(failOnError: true)
		adminRoles.each {
			new UserRole(user: admin, role: it).save(failOnError: true)
		}
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
