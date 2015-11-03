package cern.ais.gridwars

import cern.ais.gridwars.Match.Status
import cern.ais.gridwars.security.User

class MatchmakingService
{
  def configService
  boolean inPanic

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

  int score(Agent a) {
    (getMatchesOfAgent(a).sum { it.winner == a ? 3 : it.winner == null ? 1 : 0 } ?: 0) as int
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
      def match = matches.get(Math.random() * pendingMatches.size() as int)

      return match // TODO match status - pending.
    }
    catch (any) {
      log.error("Error while matchmaking!", any)
      null
    }
  }

  def cancelMatches(Agent agent) {
    getAllMatchesOfAgent(agent).each {
      it.status = Status.CANCELED
      it.save(failOnError: true)
    }
  }

  def prepareMatches(Agent agent) {
    activeAgents.each { agent2 ->
      if (agent != agent2) {
        (configService?.numMatches ?: 1).times {
          new Match(player1: agent, player2: agent2).save()
        }
      }
    }
  }
}
