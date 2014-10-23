package cern.ais.gridwars

import cern.ais.gridwars.bot.PlayerBot
import cern.ais.gridwars.command.MovementCommand
import cern.ais.gridwars.servlet.util.ClassUtils
import cern.ais.gridwars.util.OutputSwitcher
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

import java.nio.ByteBuffer

class GameExecutionService
{
  def grailsApplication

  def executeGame(Match match)
  {
    List<Player> playerList = []
    int i = 0

    for (MatchPlayer matchPlayer : match.players.sort { it.agent.team.id })
    {
      Class classToLoad = ClassUtils.loadClassFromJarFile(matchPlayer.agent.fqClassName, grailsApplication.config.cern.ais.gridwars.fileprotocol + matchPlayer.agent.jarPath, Thread.currentThread().getContextClassLoader())

      PlayerBot bot = null
      FileOutputStream newOut = null

      if (classToLoad)
      {
        try
        {
          newOut = new FileOutputStream("${grailsApplication.config.cern.ais.gridwars.basedir}player-outputs/" + matchPlayer.outputFileName)
          OutputSwitcher.instance.switchToFile(newOut)
        }
        catch (FileNotFoundException e)
        {
          OutputSwitcher.instance.restoreInitial()
          e.printStackTrace()
        }

        // Instantiate the bot in a new thread to check for time-out
        Thread playerThread = new Thread() {
          @Override
          public void run()
          {
            try
            {
              bot = (PlayerBot) classToLoad.newInstance();
            }
            catch (InstantiationException e)
            {
              OutputSwitcher.instance.restoreInitial()
              e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
              OutputSwitcher.instance.restoreInitial()
              e.printStackTrace();
            }
          }
        };

        playerThread.start();
        playerThread.join(GameConstants.INSTANTIATION_TIMEOUT_DURATION_MS);

        if (playerThread.isAlive())
        {
          playerThread.stop();
        }

        OutputSwitcher.instance.restoreInitial()

      }

      // We add a null if not instantiated!
      playerList.add(new Player(matchPlayer.agent.team.username, bot, newOut, i++));
    }

    Game game = new Game(playerList, new Game.TurnCallback() {
      @Override
      void onPlayerResponse(Player player, int turn, List<MovementCommand> movementCommands, ByteBuffer binaryGameStatus)
      {
        MatchPlayer matchPlayer = match.players.find { it.agent.team.username.equals(player.name) }
        def gameStatus = new GameStatus(imageData: binaryGameStatus.array()).save(failOnError: true) //, content: getTurnJSON(player, movementCommands)).save(failOnError: true)
        new Turn(number: turn, matchPlayer: matchPlayer, status: gameStatus).save(failOnError: true)
      }
    })

    game.startUp()

    while (!game.done())
    {
      game.nextTurn()
    }

    Player winner = game.winner

    if (!winner)
    {
      println "Game ended in draw."
      match.players.each {
        it.outcome = Outcome.DRAW
      }
    }
    else
    {
      println "Game ended, ${winner.name} won"
      match.players.each {
        if (it.agent.team.username.equals(winner.name))
        {
          it.outcome = Outcome.WIN
        }
        else
        {
          it.outcome = Outcome.LOSS
        }
      }
    }

    for (MatchPlayer matchPlayer : match.players)
    {
      matchPlayer.save(failOnError: true)
    }

    match.running = false
    match.save(failOnError: true)
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
