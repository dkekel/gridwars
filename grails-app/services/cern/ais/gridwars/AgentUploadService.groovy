package cern.ais.gridwars

import cern.ais.gridwars.bot.PlayerBot
import cern.ais.gridwars.servlet.util.ClassUtils
import org.springframework.web.multipart.commons.CommonsMultipartFile

class AgentUploadService
{
  def grailsApplication

  public boolean processJarUpload(CommonsMultipartFile uploadedFile, String agentFQCN, User user)
  {
    user.refresh()
    File destinationFile = new File("${grailsApplication.config.cern.ais.gridwars.basedir}player-jars/${user.username}_${new Date().format("yyyyMMddHHmmss")}_${uploadedFile.originalFilename}")
    uploadedFile.transferTo(destinationFile)
    println destinationFile.absolutePath
    println agentFQCN
    println user.username

    // Validate the uploaded file
    if (!validate(destinationFile, agentFQCN))
    {
      destinationFile.delete()
      return false
    }

    def agent = new Agent(jarPath: destinationFile.absolutePath, fqClassName: agentFQCN, uploadDate: new Date())

    // Invalidate previous
    user.agents?.each { it.active = false }

    user.addToAgents(agent)

    user.save()

    return true
  }

  private boolean validate(File file, String agentFQCN)
  {

    if (file.length() > 10485760)
    {
      println "Too big"
      return false
    }

    Class classToLoad = ClassUtils.loadClassFromJarFile(agentFQCN, grailsApplication.config.cern.ais.gridwars.fileprotocol + file.absolutePath, Thread.currentThread().getContextClassLoader())

    if (!classToLoad)
    {
      println "Failed to load class"
      return false
    }

    if (classToLoad.interface)
    {
      println "It's an interface"
      return false
    }

    if (!PlayerBot.class.isAssignableFrom(classToLoad))
    {
      println "Does not implement PlayerBot"
      return false
    }

    return true
  }
}
