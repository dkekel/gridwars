package cern.ais.gridwars

import cern.ais.gridwars.security.User

class Agent
{
  String  jarPath
  String  fqClassName
  Date    uploadDate
  double  eloScore = 1000
  boolean active = true

  static belongsTo = [team: User]

  static hasMany = [matches: MatchPlayer]

  static constraints = {
  }

  public int getScore()
  {
    return matches.count { it.match.players.agent.flatten().every { it.active } && it.outcome.equals(Outcome.WIN) } * 3 + matches.count { it.match.players.agent.flatten().every { it.active } && it.outcome.equals(Outcome.DRAW) }
  }
}
