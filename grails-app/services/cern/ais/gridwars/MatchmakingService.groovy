package cern.ais.gridwars

class MatchmakingService
{

  /**
   * Generates next match to be played
   * @return the match object
   */
  public Match getNextMatch()
  {
    def final activeMatchesClosure = { it.match.players.agent.flatten().every { it.active } }

    // Get active agents and sort them by their number of matches against other active agents
    List<Agent> activeAgents = Agent.findAllByActive(true).sort {
      it.matches?.count(activeMatchesClosure) ?: 0
    }

    // Not enough agents to play
    if (activeAgents.size() < 2)
    {
      return null
    }

    if ((activeAgents.first()?.matches?.count(activeMatchesClosure) ?: 0) >= GameConstants.MAXIMUM_GAMES_PER_OPPONENT * (activeAgents.size() - 1))
    {
      return null
    }

    // Get the one with the least games played
    Agent player1 = activeAgents.first()
    activeAgents.remove(player1)

    // Get agent other than this one with the least amount of games played against it
    activeAgents = activeAgents.sort { it?.matches?.match?.flatten()?.count { it.players.agent.flatten().contains(player1) } ?: 0 }
    Agent player2 = activeAgents.first()

    println "New match between ${player1.fqClassName} and ${player2.fqClassName}"

    def match = new Match(running: true)
    match.addToPlayers(new MatchPlayer(agent: player1, outputFileName: "${player1.fqClassName}_${new Date().format("yyyyMMddHHmmss")}.txt"))
            .addToPlayers(new MatchPlayer(agent: player2, outputFileName: "${player2.fqClassName}_${new Date().format("yyyyMMddHHmmss")}.txt"))
    return match.save(failOnError: true, validate: true, flush: true)
  }
}
