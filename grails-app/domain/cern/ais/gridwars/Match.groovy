package cern.ais.gridwars

class Match
{
  enum Status {
    PENDING, // MATCH IS CREATED
    RUNNING, // MATCH IS CURRENTLY EVALUATED
    SUCCEDED, // MATCH FINISHED
    FAILED, // MATCH FAILED
    CANCELED // MATCH CANCELED(NEW AGENT UPLOADED)
  }
  
  Status status = Status.PENDING
  Agent player1
  Agent player2

  Agent winner
  int turns
  Date startDate = new Date()

  static constraints = {
    winner nullable: true
    turns nullable: true
  }

  static mapping = {
    table 'game'
  }
}
