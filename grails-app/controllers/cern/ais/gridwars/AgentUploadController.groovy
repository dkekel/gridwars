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
  }

  def uploadRestricted() {
  }

  def upload()
  {
    if (!SpringSecurityUtils.ifAllGranted("ROLE_ADMIN") && !configService.capabilities.upload) {
      redirect(action: "uploadRestricted")
      return
    }

    if (!agentUploadService.processJarUpload(request.getFile("file") as CommonsMultipartFile, request.getParameter("fqcn"), springSecurityService.currentUser as User))
      render 'There have been some problems with your upload. Make sure it implements the correct interface, the FQCN is correct and it is a valid JAR file.'
    else
      redirect(controller: 'game')
  }
}
