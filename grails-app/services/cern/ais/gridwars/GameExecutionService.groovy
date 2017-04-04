package cern.ais.gridwars

import cern.ais.gridwars.Game.TurnCallback
import cern.ais.gridwars.Match.Status
import cern.ais.gridwars.command.MovementCommand

import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeoutException

class GameExecutionService
{
  static transactional = false

  def configService
  def gameSerializationService
  def fileSystemService

  static RuntimeMain runtime
  static @Lazy ExecutorService executors = { Executors.newFixedThreadPool(1) }()
  private int TIME_OUT = 60 * 1000

  synchronized def executeGame(Match match)
  {
	  if (!configService.spawnWorkers) {
		  def sM = new StartMatch(match.id, playerData(match.player1), playerData(match.player2))
		  println "New match between ${ match.player1.team.username } and ${ match.player2.team.username }"
		  println "Output will go to ${ match.player1.id } and ${ match.player2.id }"
		  match.status = Status.RUNNING
		  match.save(failOnError: true)
		  def gameFuture = executors.submit({
			  try {
			  def turns = new PriorityQueue<TurnInfo>()
			  def players = PlayerUtil.prepare(sM)
			  long time = System.currentTimeMillis()
			  def game = new Game(players, new TurnCallback() {
				  @Override void onPlayerResponse(Player player, int turn, List<MovementCommand> list,
				                                  ByteBuffer binaryGameStatus)
				  {
					  println("Processin turn: $turn of $match.id.")
					  turns.add(new TurnInfo(binaryGameStatus.array(), turn, player.id))
				  }
			  } as TurnCallback)

			  println("Geme is starting-up.")
			  game.startUp()
			  int numExceptionsInARow = 0
			  while (!game.done() && numExceptionsInARow < 10)
			  {
				  try
				  {
					  if (System.currentTimeMillis() - time > TIME_OUT)
						  throw new TimeoutException()
					  game.nextTurn()
					  numExceptionsInARow = 0;
				  }
				  catch (any)
				  {
					  numExceptionsInARow++;
					  println("Exception N($numExceptionsInARow) during simulation of $game.currentTurn. $any.message")
				  }
			  }

			  players*.outputStream*.close()
			  println("Game simulation takes ${ ((System.currentTimeMillis() - time) / 1000 as Double).round(2) } s.")
			  if (game && players && game.done())
				  println("Fin. Game $match.id finished. $game.winner won!")
			  else
				  println("Fin. Game $match.id failed!")

			  def results = new MatchResults(match.id, sM.playerData1.player, Worker.trimOutput(players?.get(0)?.outputFile?.bytes),
				  sM.playerData2.player, Worker.trimOutput(players?.get(1)?.outputFile?.bytes), game?.winner?.id, game ? true : false)

			  gameSerializationService.save(match.id, turns, results)
			  println("$match.id ${ results.isComplete ? "finished!" : "failed!" }")
			  }
			  catch (any) {
				  any.printStackTrace()
			  }
		  } as Runnable)
		  println("$gameFuture has been started")

		  return;
	  }

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
      println("No slots available. Waiting!")
    }
  }

  PlayerData playerData(Agent player) {
    new PlayerData(fileSystemService.jarFile(player.jarPath).bytes, player.id, player.fqClassName)
  }

  def cleanup() {
    runtime?.cleanup()
  }
}
