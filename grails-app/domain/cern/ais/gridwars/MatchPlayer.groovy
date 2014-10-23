package cern.ais.gridwars

class MatchPlayer
{

  Outcome outcome = Outcome.GAME_NOT_FINISHED
  String outputFileName

  static belongsTo   = [agent: Agent, match: Match]
  static hasMany     = [turns: Turn]
  static constraints = {
  }
}
