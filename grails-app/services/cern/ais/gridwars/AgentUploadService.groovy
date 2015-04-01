package cern.ais.gridwars

import cern.ais.gridwars.bot.PlayerBot
import cern.ais.gridwars.security.User
import cern.ais.gridwars.servlet.util.ClassUtils
import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.commons.CommonsMultipartFile

class AgentUploadService
{
  def grailsApplication
  def fileSystemService
  def matchmakingService

  public boolean processJarUpload(CommonsMultipartFile uploadedFile, String agentFQCN, User user) {
    user.refresh()
    File destinationFile = fileSystemService.jarFile(createJarName(user))
    uploadedFile.transferTo(destinationFile)
    log.debug("File uploaded: user:($user.username) file: $destinationFile.absolutePath($agentFQCN)")
    processUploadedFile(destinationFile, agentFQCN, user)
  }

  public boolean processJarUpload(File uploadedFile, String agentFQCN, User user) {
    user.refresh()
    def destFile = fileSystemService.jarFile(createJarName(user))
    FileUtils.copyFile(uploadedFile, destFile)
    log.debug("File uploaded: user:($user.username) file: $destFile.absolutePath($agentFQCN)")
    processUploadedFile(destFile, agentFQCN, user)
  }

  String createJarName(User user) {
    "${user.username}_${new Date().format("yyyyMMddHHmmss")}"
  }

  public boolean processUploadedFile(File file, String agentFQCN, User user) {
    // Validate the uploaded file
    if (!validate(file, agentFQCN))
    {
      file.delete()
      return false
    }

    Agent.findAllByTeamAndActive(user, true).each {
      matchmakingService.cancelMatches(it)
    }

    // Invalidate previous
    user.agents?.each {
      it.active = false;
      it.save()
    }

    def agent = new Agent(jarPath: file.absolutePath, fqClassName: agentFQCN, uploadDate: new Date())
    agent.save(flush: true)
    user.addToAgents(agent)
    user.save(flush: true)
    matchmakingService.prepareMatches(agent)

    return true
  }

  private boolean validate(File file, String agentFQCN)
  {
    def logError = { log.debug "$file($agentFQCN) haven't pass validation. Reason: $it" }
    if (file.length() > 10485760)
    {
      logError("Too big")
      return false
    }

    Class classToLoad = ClassUtils.loadClassFromJarFile(agentFQCN, grailsApplication.config.cern.ais.gridwars.fileprotocol + file.absolutePath, Thread.currentThread().getContextClassLoader())

    if (!classToLoad)
    {
      logError "Failed to load class"
      return false
    }

    if (classToLoad.interface)
    {
      logError "It's an interface"
      return false
    }

    if (!PlayerBot.class.isAssignableFrom(classToLoad))
    {
      logError "Does not implement PlayerBot"
      return false
    }

    return true
  }
}
