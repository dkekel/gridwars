package cern.ais.gridwars

class Match
{
  boolean running
  Agent winner
  int turns
  Date startDate = new Date()

  Agent player1
  Agent player2

  static constraints = {
    winner nullable: true
    turns nullable: true
  }

  static mapping = {
    table 'game'
  }
}
