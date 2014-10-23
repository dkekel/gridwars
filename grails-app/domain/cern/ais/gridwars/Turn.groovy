package cern.ais.gridwars

class Turn
{
  int number
  GameStatus status

  static belongsTo = [matchPlayer: MatchPlayer]

  static constraints = {
  }


}
