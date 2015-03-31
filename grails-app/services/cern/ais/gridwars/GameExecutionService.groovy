package cern.ais.gridwars

import cern.ais.gridwars.Match.Status

class GameExecutionService
{
  static transactional = false

  def configService
  def gameSerializationService

  static RuntimeMain runtime

  synchronized def executeGame(Match match)
  {
    if (!runtime) {
      try {
        runtime = new RuntimeMain(configService.workers.classpath as String, configService.workers.home as String, configService.workers.managerPort as int)
      }
      catch (any) {
        println("an exception occur when initializing RuntimeMain.")
        any.printStackTrace()
      }
    }
    if (runtime.slotAvailable) {
      match.status = Status.RUNNING
      match.save(failOnError: true)
      runtime.send(new StartMatch(match.id, playerData(match.player1), playerData(match.player2))) { Collection<TurnInfo> data, MatchResults results ->
        gameSerializationService.save(match.id, data, results)
        println("$match.id finished!")
      }
    }
  }

  PlayerData playerData(Agent player) {
    new PlayerData(new File(player.jarPath).bytes, player.id, player.fqClassName)
  }
}
