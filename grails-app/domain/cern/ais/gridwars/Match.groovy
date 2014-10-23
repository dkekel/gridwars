package cern.ais.gridwars

class Match
{
  boolean running
  Date startDate = new Date()

  static hasMany = [players: MatchPlayer]

  static constraints = {
  }

  static mapping = {
    table 'game'
  }
}
