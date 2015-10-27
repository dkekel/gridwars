package cern.ais.gridwars

import cern.ais.gridwars.Match.Status
import cern.ais.gridwars.security.User
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Environment

class GameController
{
  def matchmakingService
  def gameExecutionService
  def fileSystemService
  def springSecurityService

  def index()
  {
    // Get scoreboard
    [agents: Agent.findAllByActive(true).sort { matchmakingService.score(it) }, service: matchmakingService, currentLoggedInUserId: (springSecurityService.currentUser as User).id]
  }

  def list()
  {
    [games: Match.findAllByStatus(Status.SUCCEDED).sort { it.startDate }, currentLoggedInUserId: (springSecurityService.currentUser as User).id]
  }

  def view(Long id)
  {
    println("Current environment: ${Environment.current}")
    [game: Match.get(id), currentLoggedInUserId: (springSecurityService.currentUser as User).id, rootPath: Environment.current == Environment.PRODUCTION ? '' : "gridwars_server/"]
  }

  def playerOutput() {
    if (SpringSecurityUtils.ifAllGranted('ROLE_ADMIN') || (springSecurityService.currentUser as User).id == Agent.get(params.bot as long)?.team?.id) {
      def file = fileSystemService.outputFile(params.game as long, params.bot as long)
      def text = file?.exists() ? file?.text : null
      text = text ?: "No output"
      render "<pre>$text</pre>"
    }
    else
      render "No access"
  }

  def match()
  {
    def match = matchmakingService.nextMatch
    if (match)
      gameExecutionService.executeGame(match)
    redirect(action: "list")
  }
}
