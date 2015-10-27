package cern.ais.gridwars

import cern.ais.gridwars.security.User
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.web.multipart.commons.CommonsMultipartFile

class AgentUploadController
{
  def agentUploadService
  def configService
  def springSecurityService

  def index()
  {
    if (!SpringSecurityUtils.ifAllGranted("ROLE_ADMIN") && !configService.capabilities.upload)
      redirect(action: "uploadRestricted")
    else
      [error: params.error, agents: SpringSecurityUtils.ifAllGranted("ROLE_ADMIN") ? Agent.list() : Agent.findAllByTeam(springSecurityService.currentUser as User) ] 
  }

  def uploadRestricted() {
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
