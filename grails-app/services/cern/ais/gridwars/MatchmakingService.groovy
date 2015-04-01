package cern.ais.gridwars

import cern.ais.gridwars.Match.Status
import cern.ais.gridwars.security.User

class MatchmakingService
{
  List<Match> getPendingMatches() {
    Match.findAllByStatus(Status.PENDING)
  }

  public List<Agent> getActiveAgents() {
    User.list()
      .collect { Agent.findAllByTeamAndActive(it, true) }
      .grep { List<Agent> a -> !a.empty }.collect { it.first() }
  }

  public List<Agent> getFighters(List<Agent> agentList) {
    if (agentList.size() < 2)
      return null
    else {
      def sortedByMatches = agentList.sort(false) { Match.countByPlayer1(it) + Match.countByPlayer2(it) }
      [sortedByMatches[0], sortedByMatches[1]]
    }
  }

  def wins(Agent a) {
    getMatchesOfAgent(a).count { it.winner == a }
  }

  def draws(Agent a) {
    getMatchesOfAgent(a).count { it.winner == null }
  }

  def losses(Agent a) {
    getMatchesOfAgent(a).count { it.winner != null && it.winner != a }
  }

  def score(Agent a) {
    getMatchesOfAgent(a).sum { it.winner == a ? 3 : it.winner == null ? 1 : 0 }
  }

  /**
   * Returns a list of succeded matches played by agent.
   * @param a
   * @return
   */
  public List<Match> getMatchesOfAgent(Agent a) {
    Match.findAllByStatusAndPlayer1(Status.SUCCEDED, a) + Match.findAllByStatusAndPlayer2(Status.SUCCEDED, a)
  }

  public List<Match> getAllMatchesOfAgent(Agent a) {
    Match.findAllByPlayer1OrPlayer2(a, a)
  }

  public synchronized Match getNextMatch() {
    try
    {
      def matches = pendingMatches
      if (!matches)
        return null
      def match = matches.first()

      return match // TODO match status - pending.
    }
    catch (any) {
      log.error("Error while matchmaking!", any)
      null
    }
  }

//  /**
//   * Generates next match to be played
//   * @return the match object
//   */
//  public Match getNextMatch()
//  {
//    def final activeMatchesClosure = { it.match.players.agent.flatten().every { it.active } }
//
//    // Get active agents and sort them by their number of matches against other active agents
//    List<Agent> activeAgents = Agent.findAllByActive(true).sort {
//      it.matches?.count(activeMatchesClosure) ?: 0
//    }
//
//    // Not enough agents to play
//    if (activeAgents.size() < 2)
//    {
//      return null
//    }
//
//    if ((activeAgents.first()?.matches?.count(activeMatchesClosure) ?: 0) >= GameConstants.MAXIMUM_GAMES_PER_OPPONENT * (activeAgents.size() - 1))
//    {
//      return null
//    }
//
//    // Get the one with the least games played
//    Agent player1 = activeAgents.first()
//    activeAgents.remove(player1)
//
//    // Get agent other than this one with the least amount of games played against it
//    activeAgents = activeAgents.sort { it?.matches?.match?.flatten()?.count { it.players.agent.flatten().contains(player1) } ?: 0 }
//    Agent player2 = activeAgents.first()
//
//    println "New match between ${player1.fqClassName} and ${player2.fqClassName}"
//
//    def match = new Match(running: true)
//    match.addToPlayers(new MatchPlayer(agent: player1, outputFileName: "${player1.fqClassName}_${new Date().format("yyyyMMddHHmmss")}.txt"))
//            .addToPlayers(new MatchPlayer(agent: player2, outputFileName: "${player2.fqClassName}_${new Date().format("yyyyMMddHHmmss")}.txt"))
//    return match.save(failOnError: true, validate: true, flush: true)
//  }
  def cancelMatches(Agent agent)
  {
    getAllMatchesOfAgent(agent).each {
      it.status = Status.CANCELED
      it.save(failOnError: true)
    }
  }

  def prepareMatches(Agent agent) {
    activeAgents.each {
      if (agent != it)
        new Match(player1: agent, player2: it).save()
    }
  }
}
