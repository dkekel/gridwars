package cern.ais.gridwars

import cern.ais.gridwars.security.User
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.web.multipart.commons.CommonsMultipartFile

class AgentUploadController
{
  def agentUploadService
  def configService
  def springSecurityService
  def fileSystemService

  def index()
  {
    if (!SpringSecurityUtils.ifAllGranted("ROLE_ADMIN") && !configService.capabilities.upload)
      redirect(action: "uploadRestricted")
    else
      [error: params.error, agents: SpringSecurityUtils.ifAllGranted("ROLE_ADMIN") ? Agent.findAllByActive(true) : Agent.findAllByTeam(springSecurityService.currentUser as User) ] 
  }

  def uploadRestricted() {
  }

  def download() {
    String name = params.id
    if (!name) {
      response.status = 404
      return
    }

    response.setContentType("application/octet-stream")
    response.setHeader("Content-disposition", "attachment;filename=\"$name\"")
    response.outputStream << fileSystemService.jarFile(name).bytes
  }

  def upload() {
    if (!SpringSecurityUtils.ifAllGranted("ROLE_ADMIN") && !configService.capabilities.upload) {
      redirect(action: "uploadRestricted")
      return
    }

    try {
      agentUploadService.processJarUpload(request.getFile("file") as CommonsMultipartFile, request.getParameter("fqcn"), springSecurityService.currentUser as User)
      redirect(controller: 'game')
    }
    catch (JarValidationError e) {
      redirect(params: [error: e.message])
    }
  }
}
