package cern.ais.gridwars

class MatchJob
{
  def                      concurrent = false
  def MatchmakingService   matchmakingService
  def GameExecutionService gameExecutionService

  static triggers = {
    simple repeatInterval: 1000l // execute job once in 5 seconds
  }

  def execute()
  {
    // execute job
    Match match = matchmakingService.nextMatch

    if (match)
    {
      gameExecutionService.executeGame(match)
    }
  }
}
