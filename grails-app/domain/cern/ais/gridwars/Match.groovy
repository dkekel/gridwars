package cern.ais.gridwars

class Match
{
  boolean running
  Agent winner
  int turns
  Date startDate = new Date()

  static hasMany = [players: MatchPlayer]

  static constraints = {
    winner nullable: true
    turns nullable: true
  }

  static mapping = {
    table 'game'
  }
}
