package cern.ais.gridwars

import cern.ais.gridwars.security.User
import grails.plugin.springsecurity.SpringSecurityUtils

class GameController
{
  def matchmakingService
  def gameExecutionService
  def fileSystemService
  def springSecurityService

  def index()
  {
    // Get scoreboard
    [agents: Agent.findAllByActive(true).sort { a, b -> (b.score <=> a.score) }]
  }

  def list()
  {
    [games: Match.findAllByRunning(false).findAll { it.players.agent.flatten().every { it.active } }.sort { it.startDate }]
  }

  def view(Long id)
  {
    [game: Match.get(id), currentLoggedInUserId: (springSecurityService.currentUser as User).id]
  }

  def playerOutput() {
    if (SpringSecurityUtils.ifAllGranted('ROLE_ADMIN') || (springSecurityService.currentUser as User).id == params.player as long) {
      def text = fileSystemService.outputFile(Match.get(params.game as long).players.find {
        it.agent.team.id == (params.player as long)
      }.outputFileName).text
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
  }
}
