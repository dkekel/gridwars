package cern.ais.gridwars

class AgentUploadController
{
  def agentUploadService

  def index()
  {
    if (!session.user)
      redirect(controller: 'main')
  }

  def upload()
  {
    println "Sorry, no upload is possible at the moment"
    response.status = 403
    if (session.user)
    {
      if (!agentUploadService.processJarUpload(request.getFile("file"), request.getParameter("fqcn"), session.user))
      {
        render 'There have been some problems with your upload. Make sure it implements the correct interface, the FQCN is correct and it is a valid JAR file.'
      }
      else
      {
        redirect(controller: 'main')
      }
    }
    else
    {
      redirect(controller: 'main')
    }
  }
}
