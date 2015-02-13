package cern.ais.gridwars

class GameController
{
  def matchmakingService
  def gameExecutionService

  def index()
  {
    if (!session.user)
      redirect(controller: 'main')
    // Get scoreboard
    [agents: Agent.findAllByActive(true).sort { a, b -> (b.score <=> a.score) }]
  }

  def list()
  {
    Match match = matchmakingService.nextMatch

    if (match)
    {
      gameExecutionService.executeGame(match)
    }
    
    if (!session.user)
      redirect(controller: 'main')
    [games: Match.findAllByRunning(false).findAll { it.players.agent.flatten().every { it.active } }.sort { it.startDate }]
  }

  def view(Long id)
  {
    if (!session.user)
      redirect(controller: 'main')

    if (!id)
    {
      redirect(controller: 'main')
    }

    [game: Match.get(id)]
  }

}
