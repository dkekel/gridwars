package cern.ais.gridwars

import cern.ais.gridwars.command.MovementCommand
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

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
      match.running = true
      runtime.send(new StartMatch(match.id, playerData(match.players.first()), playerData(match.players.last()))) { Collection<TurnInfo> data ->
        gameSerializationService.save(match.id, data)
        println("$match.id finished!")
      }
    }
  }

  PlayerData playerData(MatchPlayer player) {
    new PlayerData(new File(player.agent.jarPath).bytes, player.id, player.agent.fqClassName)
  }

  private static String getTurnJSON(Player player, List<MovementCommand> movementCommands)
  {
    JSONObject turnJSON = new JSONObject();
    JSONArray commandJSONArray = new JSONArray()
    for (MovementCommand movementCommand : movementCommands)
    {
      JSONObject commandJSON = new JSONObject()
      commandJSON.put("x", movementCommand.coordinatesFrom.x)
      commandJSON.put("y", movementCommand.coordinatesFrom.y)
      commandJSON.put("d", movementCommand.direction.ordinal())
      commandJSON.put("a", movementCommand.amount)
      commandJSONArray.add(commandJSON)
    }
    //return commandJSONArray.toString()
    turnJSON.put("player", player.name)
    turnJSON.put("commands", commandJSONArray)

    return turnJSON.toString()
  }

}
