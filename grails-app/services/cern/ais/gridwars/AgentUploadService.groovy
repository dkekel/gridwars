package cern.ais.gridwars
import cern.ais.gridwars.FileSystemService.SharedJarFile
import cern.ais.gridwars.bot.PlayerBot
import cern.ais.gridwars.security.User
import cern.ais.gridwars.servlet.util.ClassUtils
import groovy.transform.InheritConstructors
import org.springframework.web.multipart.commons.CommonsMultipartFile

import java.lang.reflect.Modifier

@InheritConstructors
class JarValidationError extends RuntimeException {}

class AgentUploadService
{
  def grailsApplication
  def fileSystemService
  def matchmakingService

  public void processJarUpload(CommonsMultipartFile uploadedFile, String agentFQCN, User user) {
    processUpload(uploadedFile.bytes, agentFQCN, user)
  }

  public void processJarUpload(File uploadedFile, String agentFQCN, User user) {
    processUpload(uploadedFile.bytes, agentFQCN, user)
  }

  public void processUpload(byte[] fileContents, String agentFQCN, User user) {
    if (!fileContents)
      throw new JarValidationError("File is empty.")

    SharedJarFile destinationFile = fileSystemService.storeJar(fileContents)

    user.refresh()
    log.debug("File uploaded: user:($user.username) file: $destinationFile.file.absolutePath($agentFQCN)")

    try {
      processUploadedFile(destinationFile.file, agentFQCN, user)
    }
    catch (JarValidationError e) {
      destinationFile.dispose();
      throw e;
    }
  }

  public void processUploadedFile(File file, String agentFQCN, User user) {
    // Validate the uploaded file
    validate(file, agentFQCN)

    Agent.findAllByTeamAndActive(user, true).each {
      matchmakingService.cancelMatches(it)
    }

    // Invalidate previous
    user.agents?.each {
      it.active = false;
      it.save()
    }

    def agent = new Agent(jarPath: file.name, fqClassName: agentFQCN, uploadDate: new Date())
    agent.save(flush: true)
    user.addToAgents(agent)
    user.save(flush: true)
    matchmakingService.prepareMatches(agent)
  }

  private void validate(File file, String agentFQCN) {
    def logError = { log.debug "$file($agentFQCN) haven't pass validation. Reason: $it" }
    if (file.length() > 10485760) {
      logError("Too big")
      throw new JarValidationError("File is too big. Uploaded ${ String.format("%.2f", file.length() / 1024 / 1024) } MB. Limit is 10MB.")
    }

    Class classToLoad = ClassUtils.loadClassFromJarFile(agentFQCN, grailsApplication.config.cern.ais.gridwars.fileprotocol + file.absolutePath, Thread.currentThread().contextClassLoader)

    if (!classToLoad) {
      logError "Failed to load class"
      throw new JarValidationError("Failed to load bot class. Class '$agentFQCN' is not found in given jar.")
    }

    if (classToLoad.interface ||  Modifier.isAbstract(classToLoad.modifiers)) {
      logError "It's an interface"
      throw new JarValidationError("Failed to load bot class. Class '$agentFQCN' is an interface or an abstract class.")
    }

    if (!PlayerBot.class.isAssignableFrom(classToLoad)) {
      logError "Does not implement PlayerBot"
      throw new JarValidationError("Failed to load bot class. Class '$agentFQCN' is not implementing PlayerBot.")
    }
  }
}
