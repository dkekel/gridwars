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
        runtime = new RuntimeMain(configService.workers.classpath as String, configService.workers.home as String, configService.workers.max as int, configService.workers.managerPort as int)
      }
      catch (any) {
        println("an exception occur when initializing RuntimeMain.")
        any.printStackTrace()
      }
    }

    if (runtime.slotAvailable) {
      runtime.send(new StartMatch(match.id, playerData(match.player1), playerData(match.player2))) { Collection<TurnInfo> data, MatchResults results ->
        gameSerializationService.save(match.id, data, results)
        println("$match.id ${ results.isComplete ? "finished!" : "failed!" }")
      }
      println "New match between ${ match.player1.team.username } and ${ match.player2.team.username }"
      match.status = Status.RUNNING
      match.save(failOnError: true)
    }
    else {
      log.info("No slots available. Waiting!")
    }
  }

  PlayerData playerData(Agent player) {
    new PlayerData(new File(player.jarPath).bytes, player.id, player.fqClassName)
  }

  def cleanup() {
    runtime?.cleanup()
  }
}
